package com.hisabkitab.backend.customer.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerStatementResponse {

    private Long customerId;

    private String customerName;

    private LocalDate fromDate;

    private LocalDate toDate;

    /**
     * Balance before fromDate.
     *
     * Positive = customer owes organization.
     * Negative = customer has advance credit.
     */
    private BigDecimal openingBalance;

    private BigDecimal totalDebit;

    private BigDecimal totalCredit;

    /**
     * Final balance after all transactions.
     *
     * Positive = outstanding.
     * Negative = advance credit.
     */
    private BigDecimal closingBalance;

    private List<CustomerLedgerEntryResponse> transactions;
}