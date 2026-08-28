package com.hisabkitab.backend.customer.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLedgerEntryResponse {

    private LocalDateTime date;

    /**
     * BILL, PAYMENT or ADVANCE
     */
    private String type;

    private Long referenceId;

    private String referenceNumber;

    private String description;

    /**
     * Amount customer owes us.
     */
    private BigDecimal debit;

    /**
     * Amount customer has paid.
     */
    private BigDecimal credit;

    /**
     * Running outstanding balance.
     */
    private BigDecimal balance;
}