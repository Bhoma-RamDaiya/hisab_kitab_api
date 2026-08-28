package com.hisabkitab.backend.billing.application;

import com.hisabkitab.backend.billing.domain.BillEntity;
import com.hisabkitab.backend.billing.domain.BillPaymentAllocationEntity;
import com.hisabkitab.backend.billing.domain.BillPaymentEntity;
import com.hisabkitab.backend.billing.domain.BillStatus;
import com.hisabkitab.backend.billing.domain.PaymentAllocationMethod;
import com.hisabkitab.backend.billing.infrastructure.BillPaymentAllocationRepository;
import com.hisabkitab.backend.billing.infrastructure.BillPaymentRepository;
import com.hisabkitab.backend.billing.infrastructure.BillRepository;
import com.hisabkitab.backend.billing.interfaces.dto.CustomerBalanceResponse;
import com.hisabkitab.backend.billing.interfaces.dto.CustomerPaymentRequest;
import com.hisabkitab.backend.billing.interfaces.dto.CustomerPaymentResponse;
import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.customer.infrastructure.CustomerRepository;
import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.payment.domain.CustomerCreditEntity;
import com.hisabkitab.backend.payment.infrastructure.CustomerCreditAllocationRepository;
import com.hisabkitab.backend.payment.infrastructure.CustomerCreditRepository;
import com.hisabkitab.backend.payment.interfaces.dto.PaymentAllocationRequest;
import com.hisabkitab.backend.payment.interfaces.dto.PaymentAllocationResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerPaymentServiceImpl
        implements CustomerPaymentService {

    private final BillPaymentRepository billPaymentRepository;

    private final BillPaymentAllocationRepository
            billPaymentAllocationRepository;

    private final BillRepository billRepository;

    private final CustomerRepository customerRepository;

    private final OrganizationMemberRepository
            organizationMemberRepository;

    private final SecurityUtils securityUtils;
    private final CustomerCreditRepository
            customerCreditRepository;
    private final CustomerCreditAllocationRepository
            customerCreditAllocationRepository;
    private final BillPaymentCalculationService
            billPaymentCalculationService;
    private final BillStatusService
            billStatusService;
    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @Override
    @Transactional
    public ApiResponse<CustomerPaymentResponse> createPayment(
            Long organizationId,
            Long customerId,
            CustomerPaymentRequest request) {

        /*
         * Logged-in user must be an active member
         * of this organization.
         */
        OrganizationMemberEntity currentMember =
                getCurrentOrganizationMember(
                        organizationId
                );

        OrganizationEntity organization =
                currentMember.getOrganization();

        /*
         * Customer must belong to this organization.
         */
        CustomerEntity customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                customerId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found."
                                ));

        /*
         * Payment date.
         */
        LocalDateTime paidAt =
                request.getPaidAt() != null
                        ? request.getPaidAt()
                        : LocalDateTime.now();

        /*
         * Create payment first.
         *
         * We save it immediately because
         * BillPaymentAllocationEntity references
         * this payment.
         */
        BillPaymentEntity payment =
                BillPaymentEntity.builder()
                        .organization(organization)
                        .customer(customer)
                        .amount(request.getAmount())
                        .paidAt(paidAt)
                        .paymentReference(
                                request.getPaymentReference()
                        )
                        .remarks(
                                request.getRemarks()
                        )
                        .build();

        payment =
                billPaymentRepository.save(payment);


        /*
         * Determine whether the request contains
         * explicit bill allocations.
         */
        boolean hasManualAllocations =
                request.getAllocations() != null
                        && !request.getAllocations().isEmpty();


        List<BillPaymentAllocationEntity> allocations;


        /*
         * =========================================================
         * CASE 1:
         *
         * Explicit allocations were provided by the organization.
         *
         * Explicit allocations always take priority over the
         * organization's default allocation method.
         * =========================================================
         */
        if (hasManualAllocations) {

            allocations =
                    createManualAllocations(
                            payment,
                            organization,
                            customer,
                            request.getAllocations()
                    );

        } else {

            /*
             * =====================================================
             * CASE 2:
             *
             * No allocations were provided.
             *
             * Use the organization's configured method.
             * =====================================================
             */
            PaymentAllocationMethod method =
                    organization.getPaymentAllocationMethod();


            /*
             * Safety fallback for existing organizations
             * where this value may still be null.
             */
            if (method == null) {

                method =
                        PaymentAllocationMethod
                                .LATEST_BILL_FIRST;
            }


            /*
             * If the organization requires manual allocation,
             * the request cannot be empty.
             */
            if (method ==
                    PaymentAllocationMethod.MANUAL) {

                throw new RuntimeException(
                        "Manual bill allocation is required "
                                + "for this organization's payment settings."
                );
            }


            /*
             * Automatically allocate payment according to:
             *
             * LATEST_BILL_FIRST
             * OR
             * OLDEST_BILL_FIRST
             */
            allocations =
                    createAutomaticAllocations(
                            payment,
                            customer,
                            method
                    );
        }


        /*
         * Attach allocations to payment.
         */
        payment.setAllocations(
                new ArrayList<>(allocations)
        );


        /*
         * Maintain the bidirectional relationship.
         */
        for (BillPaymentAllocationEntity allocation
                : allocations) {

            allocation.setPayment(payment);
        }


        /*
         * =========================================================
         * Calculate total amount allocated to bills.
         * =========================================================
         */
        BigDecimal allocatedAmount =
                allocations.stream()
                        .map(
                                BillPaymentAllocationEntity
                                        ::getAmount
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        /*
         * Allocation must never exceed the actual payment.
         */
        if (allocatedAmount.compareTo(
                payment.getAmount()) > 0) {

            throw new RuntimeException(
                    "Total allocated amount cannot exceed "
                            + "payment amount."
            );
        }


        /*
         * =========================================================
         * Calculate unallocated payment amount.
         *
         * Example:
         *
         * Payment       = ₹50,000
         * Allocated     = ₹40,000
         * -------------------------
         * Credit        = ₹10,000
         * =========================================================
         */
        BigDecimal creditAmount =
                payment.getAmount()
                        .subtract(allocatedAmount);


        /*
         * =========================================================
         * Create customer credit for any amount that was not
         * allocated to a bill.
         * =========================================================
         */
        if (creditAmount.compareTo(
                BigDecimal.ZERO) > 0) {

            CustomerCreditEntity credit =
                    CustomerCreditEntity.builder()
                            .organization(
                                    payment.getOrganization()
                            )
                            .customer(
                                    payment.getCustomer()
                            )
                            .payment(payment)
                            .amount(creditAmount)
                            .remainingAmount(creditAmount)
                            .createdAt(
                                    LocalDateTime.now()
                            )
                            .remarks(
                                    "Unallocated customer payment credit."
                            )
                            .build();

            customerCreditRepository.save(
                    credit
            );
        }


        /*
         * =========================================================
         * Save payment again.
         *
         * Because BillPaymentEntity has:
         *
         * @OneToMany(
         *     mappedBy = "payment",
         *     cascade = CascadeType.ALL
         * )
         *
         * the allocations will be persisted together with
         * the payment.
         * =========================================================
         */
        payment =
                billPaymentRepository.save(
                        payment
                );


        /*
         * =========================================================
         * Update the status of every bill that received payment.
         * =========================================================
         */
        for (BillPaymentAllocationEntity allocation
                : allocations) {

            billStatusService.updateBillPaymentStatus(allocation.getBill());
        }


        /*
         * =========================================================
         * Response
         * =========================================================
         */
        String message;

        if (creditAmount.compareTo(
                BigDecimal.ZERO) > 0) {

            message =
                    "Customer payment recorded successfully. "
                            + "₹"
                            + creditAmount
                            + " has been added as customer credit.";

        } else {

            message =
                    "Customer payment recorded successfully.";
        }


        return ApiResponse
                .<CustomerPaymentResponse>builder()
                .success(true)
                .message(message)
                .data(
                        toResponse(payment)
                )
                .build();
    }


    // =========================================================
    // MANUAL ALLOCATION
    // =========================================================

    private List<BillPaymentAllocationEntity>
    createManualAllocations(
            BillPaymentEntity payment,
            OrganizationEntity organization,
            CustomerEntity customer,
            List<PaymentAllocationRequest> requests) {

        List<BillPaymentAllocationEntity> allocations =
                new ArrayList<>();

        BigDecimal allocatedAmount =
                BigDecimal.ZERO;


        for (PaymentAllocationRequest request
                : requests) {

            /*
             * Bill must belong to the same organization
             * and customer.
             */
            BillEntity bill =
                    getBill(
                            organization.getId(),
                            customer.getId(),
                            request.getBillId()
                    );


            /*
             * Bill must be eligible to receive payment.
             */
            validateBillForPayment(bill);


            /*
             * Prevent the same bill from appearing
             * more than once in one payment request.
             */
            boolean alreadyInRequest =
                    allocations.stream()
                            .anyMatch(
                                    existing ->
                                            existing.getBill()
                                                    .getId()
                                                    .equals(
                                                            bill.getId()
                                                    )
                            );


            if (alreadyInRequest) {

                throw new RuntimeException(
                        "Bill "
                                + bill.getBillNumber()
                                + " is allocated more than once "
                                + "in the same payment."
                );
            }


            /*
             * Requested amount for this bill.
             */
            BigDecimal allocationAmount =
                    request.getAmount();


            /*
             * Calculate the bill's actual remaining amount.
             *
             * This includes:
             *
             * 1. Normal payment allocations
             * 2. Customer credit allocations
             */
            BigDecimal remainingBillAmount =
                    billPaymentCalculationService.getBillRemainingAmount(
                            bill
                    );


            /*
             * Allocation cannot exceed the bill's
             * actual remaining amount.
             */
            if (allocationAmount.compareTo(
                    remainingBillAmount) > 0) {

                throw new RuntimeException(
                        "Allocation amount for bill "
                                + bill.getBillNumber()
                                + " exceeds its pending amount. "
                                + "Pending amount: "
                                + remainingBillAmount
                );
            }


            /*
             * Add this allocation to the current payment.
             */
            BillPaymentAllocationEntity allocation =
                    BillPaymentAllocationEntity.builder()
                            .payment(payment)
                            .bill(bill)
                            .amount(allocationAmount)
                            .build();


            allocations.add(
                    allocation
            );


            /*
             * Keep track of total amount allocated
             * by this payment.
             */
            allocatedAmount =
                    allocatedAmount.add(
                            allocationAmount
                    );


            /*
             * Total allocations for this payment
             * can never exceed the payment itself.
             */
            if (allocatedAmount.compareTo(
                    payment.getAmount()) > 0) {

                throw new RuntimeException(
                        "Total allocated amount cannot exceed "
                                + "payment amount."
                );
            }
        }


        return allocations;
    }


    // =========================================================
    // AUTOMATIC ALLOCATION
    // =========================================================

    private List<BillPaymentAllocationEntity>
    createAutomaticAllocations(
            BillPaymentEntity payment,
            CustomerEntity customer,
            PaymentAllocationMethod method) {

        List<BillStatus> payableStatuses =
                List.of(
                        BillStatus.ISSUED,
                        BillStatus.PARTIALLY_PAID
                );


        List<BillEntity> bills;


        /*
         * Latest bill first.
         */
        if (method ==
                PaymentAllocationMethod.LATEST_BILL_FIRST) {

            bills =
                    billRepository
                            .findAllByOrganizationIdAndCustomerIdAndStatusInOrderByBillDateDesc(
                                    customer.getOrganization().getId(),
                                    customer.getId(),
                                    payableStatuses
                            );

        }

        /*
         * Oldest bill first.
         */
        else if (method ==
                PaymentAllocationMethod.OLDEST_BILL_FIRST) {

            bills =
                    billRepository
                            .findAllByOrganizationIdAndCustomerIdAndStatusInOrderByBillDateAsc(
                                    customer.getOrganization().getId(),
                                    customer.getId(),
                                    payableStatuses
                            );

        }

        /*
         * MANUAL should already have been handled
         * before reaching this method.
         */
        else {

            throw new RuntimeException(
                    "Automatic payment allocation is not available "
                            + "for manual allocation mode."
            );
        }


        /*
         * Amount of the payment still available
         * for allocation.
         */
        BigDecimal remainingPayment =
                payment.getAmount();


        List<BillPaymentAllocationEntity> allocations =
                new ArrayList<>();


        /*
         * Allocate payment according to the selected
         * organization allocation method.
         */
        for (BillEntity bill : bills) {

            if (remainingPayment.compareTo(
                    BigDecimal.ZERO) <= 0) {

                break;
            }


            /*
             * Calculate the actual remaining amount
             * of this bill.
             *
             * This includes:
             *
             * 1. Direct payment allocations
             * 2. Customer credit allocations
             */
            BigDecimal remainingBillAmount =
                    billPaymentCalculationService.getBillRemainingAmount(
                            bill
                    );


            /*
             * Bill has nothing remaining.
             */
            if (remainingBillAmount.compareTo(
                    BigDecimal.ZERO) <= 0) {

                continue;
            }


            /*
             * Allocate the smaller of:
             *
             * remaining payment
             * OR
             * remaining bill amount.
             */
            BigDecimal allocationAmount =
                    remainingPayment.min(
                            remainingBillAmount
                    );


            /*
             * Safety check.
             *
             * This should always pass because
             * allocationAmount was calculated using
             * remainingBillAmount.
             */
            if (allocationAmount.compareTo(
                    remainingBillAmount) > 0) {

                throw new RuntimeException(
                        "Allocation amount exceeds the "
                                + "remaining amount of bill "
                                + bill.getBillNumber()
                );
            }


            /*
             * Create payment allocation.
             */
            BillPaymentAllocationEntity allocation =
                    BillPaymentAllocationEntity.builder()
                            .payment(payment)
                            .bill(bill)
                            .amount(allocationAmount)
                            .build();


            allocations.add(
                    allocation
            );


            /*
             * Reduce the payment amount still
             * available for allocation.
             */
            remainingPayment =
                    remainingPayment.subtract(
                            allocationAmount
                    );
        }


        return allocations;
    }


    // =========================================================
    // UPDATE BILL PAYMENT STATUS
    // =========================================================

//    private void updateBillPaymentStatus(
//            BillEntity bill) {
//
//        /*
//         * Total amount paid through normal customer payments.
//         */
//        BigDecimal directPayments =
//                billPaymentAllocationRepository
//                        .sumAllocatedAmountByBill(
//                                bill.getId()
//                        );
//
//        if (directPayments == null) {
//            directPayments = BigDecimal.ZERO;
//        }
//
//
//        /*
//         * Total amount paid using customer credit.
//         */
//        BigDecimal creditPayments =
//                customerCreditAllocationRepository
//                        .sumCreditAllocatedToBill(
//                                bill.getId()
//                        );
//
//        if (creditPayments == null) {
//            creditPayments = BigDecimal.ZERO;
//        }
//
//
//        /*
//         * Total amount paid toward this bill.
//         *
//         * Direct payment
//         * +
//         * Customer credit
//         */
//        BigDecimal totalPaid =
//                directPayments
//                        .add(creditPayments);
//
//
//        /*
//         * Nothing has been paid.
//         */
//        if (totalPaid.compareTo(
//                BigDecimal.ZERO) <= 0) {
//
//            bill.setStatus(
//                    BillStatus.ISSUED
//            );
//        }
//
//        /*
//         * Entire bill has been paid.
//         */
//        else if (totalPaid.compareTo(
//                bill.getTotalAmount()) >= 0) {
//
//            bill.setStatus(
//                    BillStatus.PAID
//            );
//        }
//
//        /*
//         * Some amount has been paid.
//         */
//        else {
//
//            bill.setStatus(
//                    BillStatus.PARTIALLY_PAID
//            );
//        }
//
//
//        billRepository.save(bill);
//    }

    // =========================================================
    // GET CUSTOMER PAYMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CustomerPaymentResponse>>
    getCustomerPayments(
            Long organizationId,
            Long customerId) {

        getCurrentOrganizationMember(
                organizationId
        );


        CustomerEntity customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                customerId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found."
                                ));


        List<CustomerPaymentResponse> payments =
                billPaymentRepository
                        .findAllByOrganizationIdAndCustomerIdOrderByPaidAtDesc(
                                organizationId,
                                customer.getId()
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();


        return ApiResponse
                .<List<CustomerPaymentResponse>>builder()
                .success(true)
                .message(
                        "Customer payment history fetched successfully."
                )
                .data(payments)
                .build();
    }


    // =========================================================
    // GET CUSTOMER BALANCE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerBalanceResponse>
    getCustomerBalance(
            Long organizationId,
            Long customerId) {

        /*
         * Logged-in user must belong to organization.
         */
        getCurrentOrganizationMember(
                organizationId
        );


        /*
         * Customer must belong to this organization.
         */
        CustomerEntity customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                customerId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found."
                                ));


        /*
         * Total active bills.
         */
        BigDecimal totalBilled =
                billRepository.sumActiveBillsByCustomer(
                        organizationId,
                        customerId
                );

        if (totalBilled == null) {
            totalBilled = BigDecimal.ZERO;
        }


        /*
         * Total money actually received from customer.
         */
        BigDecimal totalPaid =
                billPaymentRepository.sumPaymentsByCustomer(
                        organizationId,
                        customerId
                );

        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }


        /*
         * Amount of customer's payment that is still
         * available as unused credit.
         */
        BigDecimal availableCredit =
                customerCreditRepository.sumAvailableCredit(
                        organizationId,
                        customerId
                );

        if (availableCredit == null) {
            availableCredit = BigDecimal.ZERO;
        }


        /*
         * Money actually applied against bills.
         *
         * Example:
         *
         * Total payment  = ₹50,000
         * Available credit = ₹10,000
         *
         * Paid against bills = ₹40,000
         */
        BigDecimal paidAgainstBills =
                totalPaid
                        .subtract(availableCredit)
                        .max(BigDecimal.ZERO);


        /*
         * Current amount still payable by customer.
         *
         * Example:
         *
         * Bills = ₹60,000
         * Paid against bills = ₹40,000
         *
         * Pending = ₹20,000
         */
        BigDecimal pendingBalance =
                totalBilled
                        .subtract(paidAgainstBills)
                        .max(BigDecimal.ZERO);


        CustomerBalanceResponse response =
                CustomerBalanceResponse.builder()
                        .customerId(
                                customer.getId()
                        )
                        .customerName(
                                customer.getCustomerName()
                        )
                        .totalBilled(
                                totalBilled
                        )
                        .totalPaid(
                                paidAgainstBills
                        )
                        .pendingBalance(
                                pendingBalance
                        )
                        .availableCredit(
                                availableCredit
                        )
                        .build();


        return ApiResponse
                .<CustomerBalanceResponse>builder()
                .success(true)
                .message(
                        "Customer balance fetched successfully."
                )
                .data(response)
                .build();
    }


    // =========================================================
    // BILL VALIDATION
    // =========================================================

    private void validateBillForPayment(
            BillEntity bill) {

        /*
         * Draft bills cannot receive payment.
         */
        if (bill.getStatus() ==
                BillStatus.DRAFT) {

            throw new RuntimeException(
                    "Draft bill cannot receive payment."
            );
        }


        /*
         * Cancelled bills cannot receive payment.
         */
        if (bill.getStatus() ==
                BillStatus.CANCELLED) {

            throw new RuntimeException(
                    "Cancelled bill cannot receive payment."
            );
        }


        /*
         * Calculate the actual remaining amount.
         *
         * This includes:
         *
         * 1. Direct payment allocations
         * 2. Customer credit allocations
         */
        BigDecimal pendingAmount =
                billPaymentCalculationService
                        .getBillRemainingAmount(
                                bill
                        );


        /*
         * Bill cannot receive another payment
         * if nothing remains.
         */
        if (pendingAmount.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Bill has no pending amount."
            );
        }
    }


    // =========================================================
    // GET BILL
    // =========================================================

    private BillEntity getBill(
            Long organizationId,
            Long customerId,
            Long billId) {

        return billRepository
                .findByIdAndOrganizationIdAndCustomerId(
                        billId,
                        organizationId,
                        customerId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bill not found for this customer."
                        ));
    }


    // =========================================================
    // ALLOCATED AMOUNT
    // =========================================================

    private BigDecimal getAllocatedAmountForBill(
            Long billId) {

        BigDecimal allocatedAmount =
                billPaymentAllocationRepository
                        .sumAllocatedAmountByBill(
                                billId
                        );


        return allocatedAmount != null
                ? allocatedAmount
                : BigDecimal.ZERO;
    }


    // =========================================================
    // CURRENT ORGANIZATION MEMBER
    // =========================================================

    private OrganizationMemberEntity
    getCurrentOrganizationMember(
            Long organizationId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        return organizationMemberRepository
                .findByOrganizationIdAndUserIdAndStatus(
                        organizationId,
                        currentUser.getId(),
                        MembershipStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not an active member "
                                        + "of this organization."
                        ));
    }


    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private CustomerPaymentResponse toResponse(
            BillPaymentEntity payment) {

        /*
         * Get all bill allocations belonging to this payment.
         */
        List<BillPaymentAllocationEntity> allocations =
                payment.getAllocations() != null
                        ? payment.getAllocations()
                        : new ArrayList<>();


        /*
         * Total amount allocated from this payment
         * to bills.
         */
        BigDecimal allocatedAmount =
                allocations.stream()
                        .map(
                                BillPaymentAllocationEntity
                                        ::getAmount
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        /*
         * Amount that was not allocated to any bill
         * and therefore became customer credit.
         */
        BigDecimal creditAmount =
                payment.getAmount()
                        .subtract(allocatedAmount)
                        .max(BigDecimal.ZERO);


        /*
         * Convert bill allocations to response objects.
         */
        List<PaymentAllocationResponse> allocationResponses =
                allocations.stream()
                        .map(this::toAllocationResponse)
                        .toList();


        return CustomerPaymentResponse.builder()
                .id(
                        payment.getId()
                )
                .customerId(
                        payment.getCustomer().getId()
                )
                .customerName(
                        payment.getCustomer().getCustomerName()
                )
                .amount(
                        payment.getAmount()
                )
                .paidAt(
                        payment.getPaidAt()
                )
                .paymentReference(
                        payment.getPaymentReference()
                )
                .remarks(
                        payment.getRemarks()
                )
                .allocatedAmount(
                        allocatedAmount
                )
                .creditAmount(
                        creditAmount
                )
                .allocations(
                        allocationResponses
                )
                .createdAt(
                        payment.getCreatedAt()
                )
                .build();
    }


    private PaymentAllocationResponse toAllocationResponse(
            BillPaymentAllocationEntity allocation) {

        return PaymentAllocationResponse.builder()
                .id(
                        allocation.getId()
                )
                .paymentId(
                        allocation.getPayment().getId()
                )
                .billId(
                        allocation.getBill().getId()
                )
                .billNumber(
                        allocation.getBill().getBillNumber()
                )
                .amount(
                        allocation.getAmount()
                )
                .build();
    }



}