package com.hisabkitab.backend.billing.application;

import com.hisabkitab.backend.billing.domain.BillStatus;
import com.hisabkitab.backend.buyer.infrastructure.BuyerRepository;
import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.order.domain.OrderItemEntity;
import com.hisabkitab.backend.order.domain.OrderStatus;
import com.hisabkitab.backend.payment.domain.CustomerCreditAllocationEntity;
import com.hisabkitab.backend.payment.domain.CustomerCreditEntity;
import com.hisabkitab.backend.payment.infrastructure.CustomerCreditAllocationRepository;
import com.hisabkitab.backend.payment.infrastructure.CustomerCreditRepository;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.billing.interfaces.dto.BillRequest;
import com.hisabkitab.backend.billing.interfaces.dto.BillItemResponse;
import com.hisabkitab.backend.billing.interfaces.dto.BillResponse;
import com.hisabkitab.backend.billing.domain.BillEntity;
import com.hisabkitab.backend.billing.domain.BillItemEntity;
import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.order.domain.OrderEntity;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.billing.infrastructure.BillRepository;
import com.hisabkitab.backend.customer.infrastructure.CustomerRepository;
import com.hisabkitab.backend.order.infrastructure.OrderRepository;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
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
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final SecurityUtils securityUtils;
    private final BuyerRepository buyerRepository;
//    private final PaymentAllocationRepository paymentAllocationRepository;
    private final CustomerCreditAllocationRepository
            customerCreditAllocationRepository;
    private final CustomerCreditRepository
            customerCreditRepository;
    private final BillStatusService
            billStatusService;
    private final BillPaymentCalculationService
            billPaymentCalculationService;
    // =========================================================
    // CREATE BILL
    // =========================================================

    @Override
    @Transactional
    public ApiResponse<BillResponse> createBill(
            Long organizationId,
            Long customerId,
            BillRequest request) {

        /*
         * Logged-in user must have access to this organization.
         */
        OrganizationEntity organization =
                getAccessibleOrganization(
                        organizationId
                );


        /*
         * Customer must belong to this organization.
         */
        CustomerEntity customer =
                getCustomer(
                        organization.getId(),
                        customerId
                );


        /*
         * Prevent duplicate order IDs in the request.
         */
        if (request.getOrderIds() == null ||
                request.getOrderIds().isEmpty()) {

            throw new RuntimeException(
                    "At least one order is required to create a bill."
            );
        }


        if (request.getOrderIds().stream().distinct().count()
                != request.getOrderIds().size()) {

            throw new RuntimeException(
                    "Duplicate order IDs are not allowed."
            );
        }


        List<BillItemEntity> billItems =
                new ArrayList<>();


        BigDecimal totalAmount =
                BigDecimal.ZERO;


        /*
         * Convert every completed order into bill items.
         */
        for (Long orderId : request.getOrderIds()) {

            /*
             * Order must belong to this organization
             * and customer.
             */
            OrderEntity order =
                    orderRepository
                            .findByIdAndOrganizationIdAndCustomerId(
                                    orderId,
                                    organization.getId(),
                                    customer.getId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Order "
                                                    + orderId
                                                    + " not found for this customer."
                                    ));


            /*
             * Only completed orders can be billed.
             */
            if (order.getStatus()
                    != OrderStatus.COMPLETED) {

                throw new RuntimeException(
                        "Order "
                                + orderId
                                + " is not completed and cannot be billed."
                );
            }


            /*
             * The same order cannot be billed twice.
             */
            if (isOrderAlreadyBilled(organizationId,orderId)) {

                throw new RuntimeException(
                        "Order "
                                + orderId
                                + " has already been billed."
                );
            }


            /*
             * Create bill items from order items.
             */
            for (OrderItemEntity orderItem
                    : order.getItems()) {

                if (orderItem.getAmount() == null ||
                        orderItem.getAmount()
                                .compareTo(BigDecimal.ZERO) < 0) {

                    throw new RuntimeException(
                            "Invalid amount found in order "
                                    + orderId
                    );
                }


                BillItemEntity billItem =
                        BillItemEntity.builder()
                                .order(order)
                                .itemName(
                                        orderItem
                                                .getProduct()
                                                .getName()
                                )
                                .quantity(
                                        orderItem.getQuantity()
                                )
                                .rate(
                                        orderItem.getRate()
                                )
                                .amount(
                                        orderItem.getAmount()
                                )
                                .build();


                billItems.add(
                        billItem
                );


                totalAmount =
                        totalAmount.add(
                                orderItem.getAmount()
                        );
            }
        }


        /*
         * A bill must contain at least one item.
         */
        if (billItems.isEmpty()) {

            throw new RuntimeException(
                    "Cannot create a bill without bill items."
            );
        }


        /*
         * Generate organization-specific bill number.
         */
        String billNumber =
                generateBillNumber(
                        organization.getId()
                );


        /*
         * Create bill.
         */
        BillEntity bill =
                BillEntity.builder()
                        .organization(organization)
                        .customer(customer)
                        .billNumber(billNumber)
                        .billDate(LocalDateTime.now())
                        .totalAmount(totalAmount)
                        .status(BillStatus.ISSUED)
                        .notes(request.getNotes())
                        .items(billItems)
                        .build();


        /*
         * Maintain bidirectional relationship.
         */
        for (BillItemEntity billItem : billItems) {

            billItem.setBill(bill);
        }


        /*
         * Save bill together with its items.
         */
        billRepository.save(bill);


        /*
         * Automatically consume available customer credit
         * against this newly created bill.
         */
        applyAvailableCustomerCredit(
                organization.getId(),
                customer.getId(),
                bill
        );


        /*
         * Recalculate final bill status after
         * automatic credit allocation.
         */
        billStatusService.updateBillPaymentStatus(
                bill
        );


        return ApiResponse
                .<BillResponse>builder()
                .success(true)
                .message(
                        "Bill created successfully."
                )
                .data(
                        toResponse(bill)
                )
                .build();
    }

    // =========================================================
    // GET ALL BILLS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<BillResponse>> getBills(
            Long organizationId,
            Long customerId) {

        /*
         * Logged-in user must have access
         * to this organization.
         */
        OrganizationEntity organization =
                getAccessibleOrganization(
                        organizationId
                );


        /*
         * Customer must belong to this organization.
         */
        CustomerEntity customer =
                getCustomer(
                        organization.getId(),
                        customerId
                );


        /*
         * Fetch bills using BOTH organization and customer.
         *
         * This provides an additional tenant-isolation
         * check at the database query level.
         */
        List<BillResponse> bills =
                billRepository
                        .findAllByOrganizationIdAndCustomerId(
                                organization.getId(),
                                customer.getId()
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();


        return ApiResponse
                .<List<BillResponse>>builder()
                .success(true)
                .message(
                        "Bills fetched successfully."
                )
                .data(bills)
                .build();
    }

    // =========================================================
    // GET SINGLE BILL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<BillResponse> getBill(
            Long organizationId,
            Long customerId,
            Long billId) {

        /*
         * Logged-in user must have access
         * to this organization.
         */
        OrganizationEntity organization =
                getAccessibleOrganization(
                        organizationId
                );


        /*
         * Customer must belong to this organization.
         */
        CustomerEntity customer =
                getCustomer(
                        organization.getId(),
                        customerId
                );


        /*
         * Bill must belong to BOTH:
         *
         * 1. This organization
         * 2. This customer
         *
         * This provides tenant isolation directly
         * at the database query level.
         */
        BillEntity bill =
                billRepository
                        .findByIdAndOrganizationIdAndCustomerId(
                                billId,
                                organization.getId(),
                                customer.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill not found."
                                ));


        return ApiResponse
                .<BillResponse>builder()
                .success(true)
                .message(
                        "Bill fetched successfully."
                )
                .data(
                        toResponse(bill)
                )
                .build();
    }

    // =========================================================
    // CANCEL BILL
    // =========================================================

    @Override
    @Transactional
    public ApiResponse<String> cancelBill(
            Long organizationId,
            Long customerId,
            Long billId) {

        /*
         * Logged-in user must belong to organization.
         */
        getCurrentOrganizationMember(
                organizationId
        );


        /*
         * Bill must belong to this organization
         * and customer.
         */
        BillEntity bill =
                billRepository
                        .findByIdAndOrganizationIdAndCustomerId(
                                billId,
                                organizationId,
                                customerId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill not found."
                                ));


        /*
         * A bill that is already cancelled
         * cannot be cancelled again.
         */
        if (bill.getStatus() ==
                BillStatus.CANCELLED) {

            throw new RuntimeException(
                    "Bill is already cancelled."
            );
        }


        /*
         * Draft bills can be cancelled directly.
         *
         * If your business rule does not allow this,
         * remove this special case.
         */
        BigDecimal directPayments =
                billPaymentCalculationService
                        .getDirectPaymentAmount(bill.getId());

        if (directPayments == null) {
            directPayments = BigDecimal.ZERO;
        }


        /*
         * Customer credit already applied to this bill.
         */
        BigDecimal creditPayments =
                customerCreditAllocationRepository
                        .sumCreditAllocatedToBill(
                                bill.getId()
                        );

        if (creditPayments == null) {
            creditPayments = BigDecimal.ZERO;
        }


        /*
         * Total amount already paid.
         */
        BigDecimal totalPaid =
                directPayments
                        .add(creditPayments);


        /*
         * Do not allow cancellation if the bill
         * has already received any payment.
         */
        if (totalPaid.compareTo(
                BigDecimal.ZERO) > 0) {

            throw new RuntimeException(
                    "Paid or partially paid bill cannot be cancelled."
            );
        }


        /*
         * Cancel the bill.
         */
        bill.setStatus(
                BillStatus.CANCELLED
        );

        billRepository.save(bill);


        return ApiResponse
                .<String>builder()
                .success(true)
                .message(
                        "Bill cancelled successfully."
                )
                .data(
                        "Bill " +
                                bill.getBillNumber() +
                                " has been cancelled."
                )
                .build();
    }

    // =========================================================
    // REUSABLE METHODS
    // =========================================================

    private OrganizationEntity getAccessibleOrganization(
            Long organizationId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();

        OrganizationMemberEntity member =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                currentUser.getId(),
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not an active member of this organization."
                                ));

        return member.getOrganization();
    }

    private CustomerEntity getCustomer(
            Long organizationId,
            Long customerId) {

        return customerRepository
                .findByIdAndOrganizationId(
                        customerId,
                        organizationId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found."
                        ));
    }

    private boolean isOrderAlreadyBilled(
            Long organizationId,
            Long orderId) {

        return billRepository
                .existsByOrganizationIdAndItemsOrderId(
                        organizationId,
                        orderId
                );
    }
    private String generateBillNumber(
            Long organizationId) {

        long count =
                billRepository.count() + 1;

        return "BILL-" +
                organizationId +
                "-" +
                String.format(
                        "%06d",
                        count
                );
    }

    private BillResponse toResponse(
            BillEntity bill) {

        /*
         * Amount paid through normal customer payments.
         */
        BigDecimal directPayments =
                billPaymentCalculationService
                        .getDirectPaymentAmount(bill.getId());

        if (directPayments == null) {
            directPayments = BigDecimal.ZERO;
        }


        /*
         * Amount paid using customer credit.
         */
        BigDecimal creditPayments =
                customerCreditAllocationRepository
                        .sumCreditAllocatedToBill(
                                bill.getId()
                        );

        if (creditPayments == null) {
            creditPayments = BigDecimal.ZERO;
        }


        /*
         * Total amount paid toward the bill.
         */
        BigDecimal paidAmount =
                directPayments
                        .add(creditPayments)
                        .min(bill.getTotalAmount())
                        .max(BigDecimal.ZERO);


        /*
         * Remaining amount still payable.
         */
        BigDecimal pendingAmount =
                bill.getTotalAmount()
                        .subtract(paidAmount)
                        .max(BigDecimal.ZERO);


        /*
         * Bill items.
         */
        List<BillItemResponse> items =
                bill.getItems()
                        .stream()
                        .map(item ->
                                BillItemResponse.builder()
                                        .id(
                                                item.getId()
                                        )
                                        .orderId(
                                                item.getOrder() != null
                                                        ? item.getOrder().getId()
                                                        : null
                                        )
                                        .buyerName(
                                                item.getOrder() != null
                                                        && item.getOrder().getBuyer() != null
                                                        ? item.getOrder()
                                                        .getBuyer()
                                                        .getName()
                                                        : null
                                        )
                                        .itemName(
                                                item.getItemName()
                                        )
                                        .quantity(
                                                item.getQuantity()
                                        )
                                        .rate(
                                                item.getRate()
                                        )
                                        .amount(
                                                item.getAmount()
                                        )
                                        .build()
                        )
                        .toList();


        return BillResponse.builder()
                .id(
                        bill.getId()
                )
                .organizationId(
                        bill.getOrganization().getId()
                )
                .customerId(
                        bill.getCustomer().getId()
                )
                .billNumber(
                        bill.getBillNumber()
                )
                .billDate(
                        bill.getBillDate()
                )
                .totalAmount(
                        bill.getTotalAmount()
                )
                .paidAmount(
                        paidAmount
                )
                .pendingAmount(
                        pendingAmount
                )
                .status(
                        bill.getStatus()
                )
                .notes(
                        bill.getNotes()
                )
                .items(items)
                .createdAt(
                        bill.getCreatedAt()
                )
                .updatedAt(
                        bill.getUpdatedAt()
                )
                .build();
    }

    private void applyAvailableCustomerCredit(
            Long organizationId,
            Long customerId,
            BillEntity bill) {

        /*
         * Get customer's currently available credit.
         */
        BigDecimal availableCredit =
                customerCreditRepository
                        .sumAvailableCredit(
                                organizationId,
                                customerId
                        );

        if (availableCredit == null ||
                availableCredit.compareTo(
                        BigDecimal.ZERO) <= 0) {

            return;
        }


        /*
         * Calculate the actual remaining amount
         * of this bill.
         *
         * This considers:
         *
         * 1. Direct payment allocations
         * 2. Existing customer credit allocations
         */
        BigDecimal remainingBillAmount =
                billPaymentCalculationService
                        .getBillRemainingAmount(
                                bill
                        );


        /*
         * Nothing can be applied if the bill
         * is already fully paid.
         */
        if (remainingBillAmount.compareTo(
                BigDecimal.ZERO) <= 0) {

            return;
        }


        /*
         * Apply the smaller of:
         *
         * 1. Customer's available credit
         * 2. Bill's remaining amount
         */
        BigDecimal amountToApply =
                availableCredit.min(
                        remainingBillAmount
                );


        /*
         * Consume oldest credits first.
         */
        List<CustomerCreditEntity> credits =
                customerCreditRepository
                        .findAllByOrganizationIdAndCustomerIdAndRemainingAmountGreaterThanOrderByCreatedAtAsc(
                                organizationId,
                                customerId,
                                BigDecimal.ZERO
                        );


        BigDecimal remainingToApply =
                amountToApply;


        /*
         * Consume individual credit records.
         */
        for (CustomerCreditEntity credit : credits) {

            if (remainingToApply.compareTo(
                    BigDecimal.ZERO) <= 0) {

                break;
            }


            BigDecimal remainingCredit =
                    credit.getRemainingAmount();


            /*
             * Ignore empty/invalid credit records.
             */
            if (remainingCredit == null ||
                    remainingCredit.compareTo(
                            BigDecimal.ZERO) <= 0) {

                continue;
            }


            /*
             * Use the smaller of:
             *
             * requested credit remaining
             * OR
             * this credit record's remaining amount.
             */
            BigDecimal amountToUse =
                    remainingToApply.min(
                            remainingCredit
                    );


            /*
             * Create credit -> bill allocation.
             */
            CustomerCreditAllocationEntity allocation =
                    CustomerCreditAllocationEntity.builder()
                            .credit(credit)
                            .bill(bill)
                            .amount(amountToUse)
                            .allocatedAt(
                                    LocalDateTime.now()
                            )
                            .remarks(
                                    "Automatically applied customer credit."
                            )
                            .build();


            customerCreditAllocationRepository.save(
                    allocation
            );


            /*
             * Reduce remaining credit.
             */
            credit.setRemainingAmount(
                    remainingCredit.subtract(
                            amountToUse
                    )
            );


            customerCreditRepository.save(
                    credit
            );


            /*
             * Reduce amount still to be applied.
             */
            remainingToApply =
                    remainingToApply.subtract(
                            amountToUse
                    );
        }


        /*
         * Safety check.
         *
         * This should not normally happen because
         * available credit was validated before allocation.
         */
        if (remainingToApply.compareTo(
                BigDecimal.ZERO) > 0) {

            throw new RuntimeException(
                    "Unable to apply available customer credit "
                            + "to the bill."
            );
        }
    }

//    private void updateBillPaymentStatus(
//            BillEntity bill) {
//
//        BigDecimal directPayments =
//                paymentAllocationRepository
//                        .getTotalAllocatedAmountByBillId(
//                                bill.getId()
//                        );
//
//        if (directPayments == null) {
//            directPayments = BigDecimal.ZERO;
//        }
//
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
//        BigDecimal totalPaid =
//                directPayments
//                        .add(creditPayments);
//
//        if (totalPaid.compareTo(
//                BigDecimal.ZERO) <= 0) {
//
//            bill.setStatus(
//                    BillStatus.ISSUED
//            );
//
//        } else if (totalPaid.compareTo(
//                bill.getTotalAmount()) >= 0) {
//
//            bill.setStatus(
//                    BillStatus.PAID
//            );
//
//        } else {
//
//            bill.setStatus(
//                    BillStatus.PARTIALLY_PAID
//            );
//        }
//
//        billRepository.save(bill);
//    }

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
}