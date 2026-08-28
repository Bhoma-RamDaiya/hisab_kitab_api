package com.hisabkitab.backend.customer.application;

import com.hisabkitab.backend.billing.domain.BillEntity;
import com.hisabkitab.backend.billing.domain.BillPaymentAllocationEntity;
import com.hisabkitab.backend.billing.domain.BillPaymentEntity;
import com.hisabkitab.backend.billing.infrastructure.BillPaymentRepository;
import com.hisabkitab.backend.billing.infrastructure.BillRepository;
import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.customer.infrastructure.CustomerRepository;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerAccountResponse;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerLedgerEntryResponse;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerStatementResponse;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerAccountServiceImpl
        implements CustomerAccountService {

    private final BillRepository billRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final CustomerRepository customerRepository;
    private final SecurityUtils securityUtils;


    // =========================================================
    // CUSTOMER ACCOUNT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerAccountResponse> getCustomerAccount(
            Long organizationId,
            Long customerId) {

        securityUtils.getCurrentUser();

        CustomerEntity customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                customerId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found."
                                )
                        );

        List<BillEntity> bills =
                billRepository
                        .findAllByOrganizationIdAndCustomerId(
                                organizationId,
                                customerId
                        );

        List<BillPaymentEntity> payments =
                billPaymentRepository
                        .findAllByOrganizationIdAndCustomerIdOrderByPaidAtDesc(
                                organizationId,
                                customerId
                        );


        // =====================================================
        // TOTAL BILLED
        // =====================================================

        BigDecimal totalBilled =
                bills.stream()
                        .map(BillEntity::getTotalAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // TOTAL PAYMENTS RECEIVED
        // =====================================================

        BigDecimal totalPaid =
                payments.stream()
                        .map(BillPaymentEntity::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // CUSTOMER BALANCE
        //
        // totalBilled = 1000
        // totalPaid   = 700
        //
        // outstanding = 300
        //
        // totalBilled = 1000
        // totalPaid   = 1200
        //
        // outstanding = 0
        // advance     = 200
        // =====================================================

        BigDecimal difference =
                totalBilled.subtract(totalPaid);

        BigDecimal outstanding =
                difference.max(BigDecimal.ZERO);

        BigDecimal advanceCredit =
                totalPaid
                        .subtract(totalBilled)
                        .max(BigDecimal.ZERO);


        // =====================================================
        // LEDGER
        // =====================================================

        List<LedgerTransaction> transactions =
                new ArrayList<>();


        // -----------------------------------------------------
        // BILL TRANSACTIONS
        // -----------------------------------------------------

        for (BillEntity bill : bills) {

            transactions.add(
                    new LedgerTransaction(
                            bill.getBillDate(),
                            LedgerTransactionType.BILL,
                            bill.getId(),
                            bill.getBillNumber(),
                            "Bill generated",
                            bill.getTotalAmount()
                    )
            );
        }


        // -----------------------------------------------------
        // PAYMENT TRANSACTIONS
        // -----------------------------------------------------

        for (BillPaymentEntity payment : payments) {

            BigDecimal allocatedAmount =
                    payment.getAllocations()
                            .stream()
                            .map(BillPaymentAllocationEntity::getAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            BigDecimal unallocatedAmount =
                    payment.getAmount()
                            .subtract(allocatedAmount);


            // Amount actually applied to bills
            if (allocatedAmount.compareTo(BigDecimal.ZERO) > 0) {

                transactions.add(
                        new LedgerTransaction(
                                payment.getPaidAt(),
                                LedgerTransactionType.PAYMENT,
                                payment.getId(),
                                payment.getPaymentReference(),
                                "Payment received",
                                allocatedAmount
                        )
                );
            }


            // Remaining amount becomes customer credit
            if (unallocatedAmount.compareTo(BigDecimal.ZERO) > 0) {

                transactions.add(
                        new LedgerTransaction(
                                payment.getPaidAt(),
                                LedgerTransactionType.ADVANCE,
                                payment.getId(),
                                payment.getPaymentReference(),
                                "Advance payment received",
                                unallocatedAmount
                        )
                );
            }
        }


        // =====================================================
        // SORT
        // =====================================================

        transactions.sort(
                Comparator
                        .comparing(
                                LedgerTransaction::date,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .thenComparing(
                                transaction ->
                                        transaction.type()
                                                == LedgerTransactionType.BILL
                                                ? 0
                                                : 1
                        )
        );


        // =====================================================
        // RUNNING BALANCE
        // =====================================================

        BigDecimal runningBalance =
                BigDecimal.ZERO;

        List<CustomerLedgerEntryResponse> ledger =
                new ArrayList<>();


        for (LedgerTransaction transaction :
                transactions) {

            BigDecimal debit =
                    BigDecimal.ZERO;

            BigDecimal credit =
                    BigDecimal.ZERO;


            if (transaction.type()
                    == LedgerTransactionType.BILL) {

                debit =
                        transaction.amount();

                runningBalance =
                        runningBalance.add(debit);

            } else {

                credit =
                        transaction.amount();

                runningBalance =
                        runningBalance.subtract(credit);
            }


            ledger.add(
                    CustomerLedgerEntryResponse.builder()
                            .date(transaction.date())
                            .type(transaction.type().name())
                            .referenceId(
                                    transaction.referenceId()
                            )
                            .referenceNumber(
                                    transaction.referenceNumber()
                            )
                            .description(
                                    transaction.description()
                            )
                            .debit(debit)
                            .credit(credit)
                            .balance(runningBalance)
                            .build()
            );
        }


        // =====================================================
        // RESPONSE
        // =====================================================

        CustomerAccountResponse response =
                CustomerAccountResponse.builder()
                        .customerId(customer.getId())
                        .customerName(customer.getCustomerName())
                        .totalBilled(totalBilled)
                        .totalPaid(totalPaid)
                        .outstanding(outstanding)
                        .advanceCredit(advanceCredit)
                        .ledger(ledger)
                        .build();


        return ApiResponse
                .<CustomerAccountResponse>builder()
                .success(true)
                .message(
                        "Customer account fetched successfully."
                )
                .data(response)
                .build();
    }


    // =========================================================
    // CUSTOMER STATEMENT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CustomerStatementResponse> getCustomerStatement(
            Long organizationId,
            Long customerId,
            LocalDate fromDate,
            LocalDate toDate) {

        securityUtils.getCurrentUser();


        if (fromDate == null || toDate == null) {

            throw new RuntimeException(
                    "From date and to date are required."
            );
        }


        if (fromDate.isAfter(toDate)) {

            throw new RuntimeException(
                    "From date cannot be after to date."
            );
        }


        CustomerEntity customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                customerId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found."
                                )
                        );


        LocalDateTime fromDateTime =
                fromDate.atStartOfDay();

        LocalDateTime toDateTime =
                toDate
                        .plusDays(1)
                        .atStartOfDay();


        // =====================================================
        // OPENING BALANCE
        // =====================================================

        BigDecimal previousBilled =
                billRepository.getTotalBilledBeforeDate(
                        organizationId,
                        customerId,
                        fromDateTime
                );


        BigDecimal previousPaid =
                billPaymentRepository.getTotalPaidBeforeDate(
                        organizationId,
                        customerId,
                        fromDateTime
                );


        BigDecimal openingBalance =
                previousBilled.subtract(previousPaid);


        // =====================================================
        // PERIOD BILLS
        // =====================================================

        List<BillEntity> bills =
                billRepository
                        .findAllByOrganizationIdAndCustomerIdAndBillDateBetween(
                                organizationId,
                                customerId,
                                fromDateTime,
                                toDateTime
                        );


        // =====================================================
        // PERIOD PAYMENTS
        // =====================================================

        List<BillPaymentEntity> payments =
                billPaymentRepository
                        .findAllByOrganizationIdAndCustomerIdAndPaidAtBetween(
                                organizationId,
                                customerId,
                                fromDateTime,
                                toDateTime
                        );


        List<LedgerTransaction> transactions =
                new ArrayList<>();


        // -----------------------------------------------------
        // BILLS
        // -----------------------------------------------------

        for (BillEntity bill : bills) {

            transactions.add(
                    new LedgerTransaction(
                            bill.getBillDate(),
                            LedgerTransactionType.BILL,
                            bill.getId(),
                            bill.getBillNumber(),
                            "Bill generated",
                            bill.getTotalAmount()
                    )
            );
        }


        // -----------------------------------------------------
        // PAYMENTS
        // -----------------------------------------------------

        for (BillPaymentEntity payment : payments) {

            BigDecimal allocatedAmount =
                    payment.getAllocations()
                            .stream()
                            .map(BillPaymentAllocationEntity::getAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            BigDecimal unallocatedAmount =
                    payment.getAmount()
                            .subtract(allocatedAmount);


            if (allocatedAmount.compareTo(BigDecimal.ZERO) > 0) {

                transactions.add(
                        new LedgerTransaction(
                                payment.getPaidAt(),
                                LedgerTransactionType.PAYMENT,
                                payment.getId(),
                                payment.getPaymentReference(),
                                "Payment received",
                                allocatedAmount
                        )
                );
            }


            if (unallocatedAmount.compareTo(BigDecimal.ZERO) > 0) {

                transactions.add(
                        new LedgerTransaction(
                                payment.getPaidAt(),
                                LedgerTransactionType.ADVANCE,
                                payment.getId(),
                                payment.getPaymentReference(),
                                "Advance payment received",
                                unallocatedAmount
                        )
                );
            }
        }


        // =====================================================
        // SORT
        // =====================================================

        transactions.sort(
                Comparator
                        .comparing(
                                LedgerTransaction::date,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .thenComparing(
                                transaction ->
                                        transaction.type()
                                                == LedgerTransactionType.BILL
                                                ? 0
                                                : 1
                        )
        );


        // =====================================================
        // RUNNING BALANCE
        // =====================================================

        BigDecimal runningBalance =
                openingBalance;

        BigDecimal totalDebit =
                BigDecimal.ZERO;

        BigDecimal totalCredit =
                BigDecimal.ZERO;

        List<CustomerLedgerEntryResponse> ledger =
                new ArrayList<>();


        for (LedgerTransaction transaction :
                transactions) {

            BigDecimal debit =
                    BigDecimal.ZERO;

            BigDecimal credit =
                    BigDecimal.ZERO;


            if (transaction.type()
                    == LedgerTransactionType.BILL) {

                debit =
                        transaction.amount();

                totalDebit =
                        totalDebit.add(debit);

                runningBalance =
                        runningBalance.add(debit);

            } else {

                credit =
                        transaction.amount();

                totalCredit =
                        totalCredit.add(credit);

                runningBalance =
                        runningBalance.subtract(credit);
            }


            ledger.add(
                    CustomerLedgerEntryResponse.builder()
                            .date(transaction.date())
                            .type(transaction.type().name())
                            .referenceId(
                                    transaction.referenceId()
                            )
                            .referenceNumber(
                                    transaction.referenceNumber()
                            )
                            .description(
                                    transaction.description()
                            )
                            .debit(debit)
                            .credit(credit)
                            .balance(runningBalance)
                            .build()
            );
        }


        // =====================================================
        // RESPONSE
        // =====================================================

        CustomerStatementResponse response =
                CustomerStatementResponse.builder()
                        .customerId(customer.getId())
                        .customerName(
                                customer.getCustomerName()
                        )
                        .fromDate(fromDate)
                        .toDate(toDate)
                        .openingBalance(openingBalance)
                        .totalDebit(totalDebit)
                        .totalCredit(totalCredit)
                        .closingBalance(runningBalance)
                        .transactions(ledger)
                        .build();


        return ApiResponse
                .<CustomerStatementResponse>builder()
                .success(true)
                .message(
                        "Customer statement fetched successfully."
                )
                .data(response)
                .build();
    }


    // =========================================================
    // INTERNAL LEDGER TRANSACTION
    // =========================================================

    private record LedgerTransaction(
            LocalDateTime date,
            LedgerTransactionType type,
            Long referenceId,
            String referenceNumber,
            String description,
            BigDecimal amount
    ) {
    }


    private enum LedgerTransactionType {

        BILL,

        PAYMENT,

        ADVANCE
    }
}