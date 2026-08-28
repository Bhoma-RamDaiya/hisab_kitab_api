package com.hisabkitab.backend.payment.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreditAllocationRequest {

    @NotNull(message = "Credit amount is required.")
    @DecimalMin(
            value = "0.01",
            message = "Credit amount must be greater than zero."
    )
    private BigDecimal amount;

    private String remarks;
}