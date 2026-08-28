package com.hisabkitab.backend.billing.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBalanceResponse {

    private Long customerId;

    private String customerName;

    private BigDecimal totalBilled;

    private BigDecimal totalPaid;

    private BigDecimal pendingBalance;

    private BigDecimal availableCredit;
}