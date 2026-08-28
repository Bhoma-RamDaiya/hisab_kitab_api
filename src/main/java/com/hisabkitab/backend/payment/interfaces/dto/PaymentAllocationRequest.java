package com.hisabkitab.backend.payment.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAllocationRequest {

    @NotNull(message = "Bill ID is required.")
    private Long billId;

    @NotNull(message = "Allocation amount is required.")
    @Positive(message = "Allocation amount must be greater than zero.")
    private BigDecimal amount;
}