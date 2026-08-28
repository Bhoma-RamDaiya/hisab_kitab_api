package com.hisabkitab.backend.order.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemMeasurementRequest {

    @NotNull(message = "Measurement ID is required.")
    private Long measurementId;

    @NotNull(message = "Measurement value is required.")
    private BigDecimal value;
}