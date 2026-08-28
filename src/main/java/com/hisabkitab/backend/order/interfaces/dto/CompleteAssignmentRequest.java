package com.hisabkitab.backend.order.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteAssignmentRequest {

    @NotNull(message = "Completed quantity is required.")
    @PositiveOrZero(message = "Completed quantity cannot be negative.")
    private BigDecimal completedQuantity;

    @NotNull(message = "Rejected quantity is required.")
    @PositiveOrZero(message = "Rejected quantity cannot be negative.")
    private BigDecimal rejectedQuantity;

    private String notes;
}