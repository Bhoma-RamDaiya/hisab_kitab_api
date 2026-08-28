package com.hisabkitab.backend.payment.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAllocationResponse {

    private Long id;

    private Long paymentId;

    private Long billId;

    private String billNumber;

    private BigDecimal amount;
}