package com.hisabkitab.backend.payment.application;

import com.hisabkitab.backend.billing.application.BillPaymentCalculationService;
import com.hisabkitab.backend.billing.application.BillStatusService;
import com.hisabkitab.backend.billing.domain.BillEntity;
import com.hisabkitab.backend.billing.domain.BillStatus;
import com.hisabkitab.backend.billing.infrastructure.BillPaymentAllocationRepository;
import com.hisabkitab.backend.billing.infrastructure.BillRepository;
import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.customer.infrastructure.CustomerRepository;
import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.payment.domain.CustomerCreditAllocationEntity;
import com.hisabkitab.backend.payment.domain.CustomerCreditEntity;
import com.hisabkitab.backend.payment.infrastructure.CustomerCreditAllocationRepository;
import com.hisabkitab.backend.payment.infrastructure.CustomerCreditRepository;
import com.hisabkitab.backend.payment.interfaces.dto.CustomerCreditAllocationRequest;
import com.hisabkitab.backend.payment.interfaces.dto.CustomerCreditAllocationResponse;
import com.hisabkitab.backend.payment.interfaces.dto.CustomerCreditResponse;
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
public class CustomerCreditServiceImpl
        implements CustomerCreditService {

    private final CustomerCreditRepository customerCreditRepository;

    private final CustomerCreditAllocationRepository
            customerCreditAllocationRepository;

    private final CustomerRepository customerRepository;

    private final BillRepository billRepository;

    private final OrganizationMemberRepository
            organizationMemberRepository;

    private final SecurityUtils securityUtils;
    private final BillPaymentAllocationRepository
            billPaymentAllocationRepository;
    private final BillPaymentCalculationService
            billPaymentCalculationService;
    private final BillStatusService
            billStatusService;

    @Override
    @Transactional
    public ApiResponse<List<CustomerCreditAllocationResponse>>
    applyCreditToBill(
            Long organizationId,
            Long customerId,
            Long billId,
            CustomerCreditAllocationRequest request) {

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
         * Bill must belong to the same customer
         * and organization.
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
                                        "Bill not found for this customer."
                                ));


        /*
         * Draft and cancelled bills cannot receive credit.
         */
        if (bill.getStatus() ==
                BillStatus.CANCELLED) {

            throw new RuntimeException(
                    "Cancelled bill cannot receive credit."
            );
        }

        if (bill.getStatus() ==
                BillStatus.DRAFT) {

            throw new RuntimeException(
                    "Draft bill cannot receive credit."
            );
        }


        /*
         * Calculate the actual remaining amount of the bill.
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
         * Bill is already fully paid.
         */
        if (remainingBillAmount.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Bill is already fully paid."
            );
        }


        /*
         * Amount of credit requested by the user.
         */
        BigDecimal requestedAmount =
                request.getAmount();


        /*
         * Get customer's currently available credit.
         */
        BigDecimal availableCredit =
                customerCreditRepository
                        .sumAvailableCredit(
                                organizationId,
                                customerId
                        );

        if (availableCredit == null) {
            availableCredit = BigDecimal.ZERO;
        }


        /*
         * Requested credit cannot exceed
         * customer's available credit.
         */
        if (requestedAmount.compareTo(
                availableCredit) > 0) {

            throw new RuntimeException(
                    "Requested credit exceeds customer's "
                            + "available credit. "
                            + "Available credit: "
                            + availableCredit
            );
        }


        /*
         * Requested credit cannot exceed
         * the bill's remaining amount.
         */
        if (requestedAmount.compareTo(
                remainingBillAmount) > 0) {

            throw new RuntimeException(
                    "Credit amount exceeds bill's "
                            + "remaining amount. "
                            + "Remaining amount: "
                            + remainingBillAmount
            );
        }


        /*
         * Amount still left to apply.
         */
        BigDecimal remainingToApply =
                requestedAmount;


        /*
         * Keep track of every allocation created
         * during this operation.
         */
        List<CustomerCreditAllocationEntity>
                createdAllocations =
                new ArrayList<>();


        /*
         * Get available credits.
         *
         * Oldest credit is consumed first.
         */
        List<CustomerCreditEntity> credits =
                customerCreditRepository
                        .findAllByOrganizationIdAndCustomerIdAndRemainingAmountGreaterThanOrderByCreatedAtAsc(
                                organizationId,
                                customerId,
                                BigDecimal.ZERO
                        );


        /*
         * Consume credits one by one.
         */
        for (CustomerCreditEntity credit : credits) {

            if (remainingToApply.compareTo(
                    BigDecimal.ZERO) <= 0) {

                break;
            }


            BigDecimal available =
                    credit.getRemainingAmount();


            /*
             * Ignore empty/invalid credit records.
             */
            if (available == null ||
                    available.compareTo(
                            BigDecimal.ZERO) <= 0) {

                continue;
            }


            /*
             * Use the smaller of:
             *
             * remaining requested amount
             * OR
             * available amount in this credit.
             */
            BigDecimal amountToUse =
                    remainingToApply.min(
                            available
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
                                    request.getRemarks()
                            )
                            .build();


            customerCreditAllocationRepository.save(
                    allocation
            );


            /*
             * Keep allocation for response.
             */
            createdAllocations.add(
                    allocation
            );


            /*
             * Reduce remaining credit.
             */
            credit.setRemainingAmount(
                    available.subtract(
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
         */
        if (remainingToApply.compareTo(
                BigDecimal.ZERO) > 0) {

            throw new RuntimeException(
                    "Unable to apply the requested "
                            + "customer credit."
            );
        }


        /*
         * Update bill status.
         *
         * This considers both:
         *
         * direct payments
         * +
         * customer credit
         */
        billStatusService.updateBillPaymentStatus(bill);


        /*
         * Convert all newly created allocations
         * into response objects.
         */
        List<CustomerCreditAllocationResponse>
                responses =
                createdAllocations.stream()
                        .map(
                                this::toAllocationResponse
                        )
                        .toList();


        return ApiResponse
                .<List<CustomerCreditAllocationResponse>>builder()
                .success(true)
                .message(
                        "Customer credit applied successfully."
                )
                .data(responses)
                .build();
    }



    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CustomerCreditAllocationResponse>>
    getCustomerCreditHistory(
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

        List<CustomerCreditAllocationResponse> history =
                customerCreditAllocationRepository
                        .findAllByCreditOrganizationIdAndCreditCustomerIdOrderByAllocatedAtDesc(
                                organizationId,
                                customerId
                        )
                        .stream()
                        .map(this::toAllocationResponse)
                        .toList();

        return ApiResponse
                .<List<CustomerCreditAllocationResponse>>builder()
                .success(true)
                .message(
                        "Customer credit history fetched successfully."
                )
                .data(history)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerCreditResponse> getCustomerCredit(
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

        BigDecimal totalCredit =
                customerCreditRepository
                        .sumTotalCredit(
                                organizationId,
                                customerId
                        );

        if (totalCredit == null) {
            totalCredit = BigDecimal.ZERO;
        }

        BigDecimal availableCredit =
                customerCreditRepository
                        .sumAvailableCredit(
                                organizationId,
                                customerId
                        );

        if (availableCredit == null) {
            availableCredit = BigDecimal.ZERO;
        }

        BigDecimal usedCredit =
                totalCredit
                        .subtract(availableCredit)
                        .max(BigDecimal.ZERO);

        CustomerCreditResponse response =
                CustomerCreditResponse.builder()
                        .customerId(
                                customer.getId()
                        )
                        .customerName(
                                customer.getCustomerName()
                        )
                        .totalCredit(
                                totalCredit
                        )
                        .usedCredit(
                                usedCredit
                        )
                        .availableCredit(
                                availableCredit
                        )
                        .build();

        return ApiResponse
                .<CustomerCreditResponse>builder()
                .success(true)
                .message(
                        "Customer credit fetched successfully."
                )
                .data(response)
                .build();
    }


    private BigDecimal getDirectPayments(
            BillEntity bill) {

        BigDecimal directPayments =
                billPaymentAllocationRepository
                        .sumAllocatedAmountByBill(
                                bill.getId()
                        );

        return directPayments != null
                ? directPayments
                : BigDecimal.ZERO;
    }


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


    private CustomerCreditAllocationResponse toAllocationResponse(
            CustomerCreditAllocationEntity allocation) {

        return CustomerCreditAllocationResponse.builder()
                .id(
                        allocation.getId()
                )
                .creditId(
                        allocation.getCredit().getId()
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
                .allocatedAt(
                        allocation.getAllocatedAt()
                )
                .remarks(
                        allocation.getRemarks()
                )
                .build();
    }



//    private void updateBillPaymentStatus(
//            BillEntity bill) {
//
//        /*
//         * Amount paid through normal customer payments.
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
//         * Amount paid using previously created
//         * customer credit.
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
//         * Direct payments
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
//         * Some amount has been paid,
//         * but the bill is not fully paid.
//         */
//        else {
//
//            bill.setStatus(
//                    BillStatus.PARTIALLY_PAID
//            );
//        }
//
//
//        /*
//         * Persist the updated bill status.
//         */
//        billRepository.save(bill);
//    }


}