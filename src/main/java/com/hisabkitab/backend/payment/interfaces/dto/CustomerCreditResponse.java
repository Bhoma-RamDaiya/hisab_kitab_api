package com.hisabkitab.backend.payment.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreditResponse {

    private Long customerId;

    private String customerName;

    private BigDecimal totalCredit;

    private BigDecimal usedCredit;

    private BigDecimal availableCredit;
}