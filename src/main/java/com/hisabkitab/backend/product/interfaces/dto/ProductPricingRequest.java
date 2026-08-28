package com.hisabkitab.backend.product.interfaces.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPricingRequest {

    @NotNull(message = "Customer rate is required.")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Customer rate must be greater than zero."
    )
    private BigDecimal customerRate;

    @NotNull(message = "Worker rate is required.")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Worker rate must be greater than zero."
    )
    private BigDecimal workerRate;
}