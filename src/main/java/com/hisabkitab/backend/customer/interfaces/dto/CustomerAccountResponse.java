package com.hisabkitab.backend.customer.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccountResponse {

    private Long customerId;

    private String customerName;

    private BigDecimal totalBilled;

    private BigDecimal totalPaid;

    private BigDecimal outstanding;

    private BigDecimal advanceCredit;

    private List<CustomerLedgerEntryResponse> ledger;
}