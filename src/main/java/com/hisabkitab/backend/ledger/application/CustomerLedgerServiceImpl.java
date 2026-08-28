package com.hisabkitab.backend.ledger.application;

import com.hisabkitab.backend.billing.domain.BillEntity;
import com.hisabkitab.backend.billing.domain.BillPaymentAllocationEntity;
import com.hisabkitab.backend.billing.domain.BillPaymentEntity;
import com.hisabkitab.backend.billing.infrastructure.BillPaymentRepository;
import com.hisabkitab.backend.billing.infrastructure.BillRepository;
import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.customer.infrastructure.CustomerRepository;
import com.hisabkitab.backend.ledger.domain.LedgerEntryType;
import com.hisabkitab.backend.ledger.interfaces.dto.CustomerLedgerEntryResponse;
import com.hisabkitab.backend.ledger.interfaces.dto.LedgerBillReferenceResponse;
import com.hisabkitab.backend.ledger.interfaces.dto.LedgerOrderReferenceResponse;
import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.organization.infrastructure.OrganizationRepository;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerLedgerServiceImpl
        implements CustomerLedgerService {

    private final OrganizationRepository organizationRepository;

    private final CustomerRepository customerRepository;

    private final OrganizationMemberRepository
            organizationMemberRepository;

    private final SecurityUtils securityUtils;

    private final BillRepository billRepository;

    private final BillPaymentRepository billPaymentRepository;


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CustomerLedgerEntryResponse>>
    getCustomerLedger(
            Long organizationId,
            Long customerId) {

        /*
         * -----------------------------------------------------
         * 1. Logged-in user must be an active member
         *    of this organization.
         * -----------------------------------------------------
         */
        getCurrentOrganizationMember(
                organizationId
        );


        /*
         * -----------------------------------------------------
         * 2. Organization must exist.
         * -----------------------------------------------------
         */
        OrganizationEntity organization =
                organizationRepository
                        .findById(organizationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Organization not found."
                                ));


        /*
         * -----------------------------------------------------
         * 3. Customer must belong to this organization.
         * -----------------------------------------------------
         */
        CustomerEntity customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                customerId,
                                organization.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found."
                                ));


        /*
         * -----------------------------------------------------
         * 4. Fetch customer's bills.
         *
         * Organization + customer are both part of
         * the repository query.
         * -----------------------------------------------------
         */
        List<BillEntity> bills =
                billRepository
                        .findAllByOrganizationIdAndCustomerId(
                                organization.getId(),
                                customer.getId()
                        );


        /*
         * -----------------------------------------------------
         * 5. Fetch customer's payments.
         *
         * Organization + customer are both part of
         * the repository query.
         * -----------------------------------------------------
         */
        List<BillPaymentEntity> payments =
                billPaymentRepository
                        .findAllByOrganizationIdAndCustomerIdOrderByPaidAtDesc(
                                organization.getId(),
                                customer.getId()
                        );


        /*
         * -----------------------------------------------------
         * 6. Create combined ledger entries.
         * -----------------------------------------------------
         */
        List<CustomerLedgerEntryResponse> entries =
                new ArrayList<>();


        /*
         * =====================================================
         * BILL ENTRIES
         * =====================================================
         */
        for (BillEntity bill : bills) {

            /*
             * A bill may contain multiple BillItems
             * belonging to the same Order.
             *
             * We therefore deduplicate using Order ID.
             */
            Map<Long, LedgerOrderReferenceResponse>
                    orderMap =
                    new LinkedHashMap<>();


            if (bill.getItems() != null) {

                bill.getItems()
                        .stream()
                        .filter(item ->
                                item.getOrder() != null
                        )
                        .forEach(item -> {

                            Long orderId =
                                    item.getOrder().getId();

                            orderMap.putIfAbsent(
                                    orderId,
                                    LedgerOrderReferenceResponse
                                            .builder()
                                            .orderId(orderId)
                                            .build()
                            );
                        });
            }


            List<LedgerOrderReferenceResponse>
                    orders =
                    new ArrayList<>(
                            orderMap.values()
                    );


            /*
             * Create BILL ledger entry.
             */
            CustomerLedgerEntryResponse billEntry =
                    CustomerLedgerEntryResponse
                            .builder()
                            .id(
                                    bill.getId()
                            )
                            .date(
                                    bill.getBillDate()
                            )
                            .type(
                                    LedgerEntryType.BILL
                            )
                            .reference(
                                    bill.getBillNumber()
                            )
                            .description(
                                    "Bill created"
                            )
                            .debit(
                                    bill.getTotalAmount()
                            )
                            .credit(
                                    BigDecimal.ZERO
                            )
                            .balance(
                                    BigDecimal.ZERO
                            )
                            .billId(
                                    bill.getId()
                            )
                            .billNumber(
                                    bill.getBillNumber()
                            )
                            .paymentId(null)
                            .orders(
                                    orders
                            )
                            .bills(
                                    new ArrayList<>()
                            )
                            .build();


            entries.add(
                    billEntry
            );
        }


        /*
         * =====================================================
         * PAYMENT ENTRIES
         * =====================================================
         */
        for (BillPaymentEntity payment : payments) {

            /*
             * -------------------------------------------------
             * Payment reference.
             *
             * paymentReference is optional, therefore create
             * a safe fallback.
             * -------------------------------------------------
             */
            String paymentReference =
                    payment.getPaymentReference() != null
                            && !payment
                            .getPaymentReference()
                            .isBlank()
                            ? payment.getPaymentReference()
                            : "PAY-" + payment.getId();


            /*
             * -------------------------------------------------
             * Bills to which this payment was allocated.
             *
             * One payment can be allocated to multiple bills.
             * -------------------------------------------------
             */
            Map<Long, LedgerBillReferenceResponse>
                    billMap =
                    new LinkedHashMap<>();


            if (payment.getAllocations() != null) {

                for (
                        BillPaymentAllocationEntity allocation
                        : payment.getAllocations()
                ) {

                    if (allocation.getBill() == null) {
                        continue;
                    }


                    Long billId =
                            allocation
                                    .getBill()
                                    .getId();


                    /*
                     * A payment should normally contain only
                     * one allocation for the same bill because
                     * of the unique constraint:
                     *
                     * payment_id + bill_id
                     *
                     * Still, aggregate safely here.
                     */
                    LedgerBillReferenceResponse existing =
                            billMap.get(
                                    billId
                            );


                    if (existing == null) {

                        billMap.put(
                                billId,
                                LedgerBillReferenceResponse
                                        .builder()
                                        .billId(
                                                billId
                                        )
                                        .billNumber(
                                                allocation
                                                        .getBill()
                                                        .getBillNumber()
                                        )
                                        .allocatedAmount(
                                                allocation
                                                        .getAmount()
                                        )
                                        .build()
                        );

                    } else {

                        BigDecimal existingAmount =
                                existing.getAllocatedAmount() != null
                                        ? existing
                                        .getAllocatedAmount()
                                        : BigDecimal.ZERO;

                        BigDecimal allocationAmount =
                                allocation.getAmount() != null
                                        ? allocation.getAmount()
                                        : BigDecimal.ZERO;

                        existing.setAllocatedAmount(
                                existingAmount.add(
                                        allocationAmount
                                )
                        );
                    }
                }
            }


            List<LedgerBillReferenceResponse>
                    allocatedBills =
                    new ArrayList<>(
                            billMap.values()
                    );


            /*
             * -------------------------------------------------
             * Create PAYMENT ledger entry.
             *
             * The entire payment is CREDIT.
             *
             * We do NOT create a separate CREDIT_CREATED
             * entry for the unallocated portion because that
             * would double-count the customer's money.
             * -------------------------------------------------
             */
            CustomerLedgerEntryResponse paymentEntry =
                    CustomerLedgerEntryResponse
                            .builder()
                            .id(
                                    payment.getId()
                            )
                            .date(
                                    payment.getPaidAt()
                            )
                            .type(
                                    LedgerEntryType.PAYMENT
                            )
                            .reference(
                                    paymentReference
                            )
                            .description(
                                    "Customer payment received"
                            )
                            .debit(
                                    BigDecimal.ZERO
                            )
                            .credit(
                                    payment.getAmount()
                            )
                            .balance(
                                    BigDecimal.ZERO
                            )
                            .billId(null)
                            .billNumber(null)
                            .paymentId(
                                    payment.getId()
                            )
                            .orders(
                                    new ArrayList<>()
                            )
                            .bills(
                                    allocatedBills
                            )
                            .build();


            entries.add(
                    paymentEntry
            );
        }


        /*
         * =====================================================
         * SORT LEDGER
         * =====================================================
         *
         * Older transactions come first.
         *
         * If a BILL and PAYMENT have exactly the same
         * timestamp, BILL comes first.
         */
        entries.sort(
                Comparator
                        .comparing(
                                CustomerLedgerEntryResponse
                                        ::getDate,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .thenComparing(
                                entry ->
                                        entry.getType()
                                                == LedgerEntryType.BILL
                                                ? 0
                                                : 1
                        )
        );


        /*
         * =====================================================
         * CALCULATE RUNNING BALANCE
         * =====================================================
         *
         * Debit:
         *      Customer owes organization more.
         *
         * Credit:
         *      Customer has paid organization.
         *
         * Formula:
         *
         * balance =
         *      previous balance
         *      + debit
         *      - credit
         */
        BigDecimal runningBalance =
                BigDecimal.ZERO;


        for (CustomerLedgerEntryResponse entry
                : entries) {

            BigDecimal debit =
                    entry.getDebit() != null
                            ? entry.getDebit()
                            : BigDecimal.ZERO;


            BigDecimal credit =
                    entry.getCredit() != null
                            ? entry.getCredit()
                            : BigDecimal.ZERO;


            runningBalance =
                    runningBalance
                            .add(debit)
                            .subtract(credit);


            entry.setBalance(
                    runningBalance
            );
        }


        /*
         * =====================================================
         * RETURN
         * =====================================================
         */
        return ApiResponse
                .<List<CustomerLedgerEntryResponse>>builder()
                .success(true)
                .message(
                        "Customer ledger fetched successfully."
                )
                .data(
                        entries
                )
                .build();
    }


    /*
     * =========================================================
     * CURRENT ORGANIZATION MEMBER
     * =========================================================
     *
     * Makes sure the logged-in user actually belongs to
     * the organization and is still active.
     */
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
                        )
                );
    }
}