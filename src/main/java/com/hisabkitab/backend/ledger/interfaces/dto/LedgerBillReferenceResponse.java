package com.hisabkitab.backend.ledger.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerBillReferenceResponse {

    private Long billId;

    private String billNumber;

    private BigDecimal allocatedAmount;
}