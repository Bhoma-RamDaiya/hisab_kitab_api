package com.hisabkitab.backend.order.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemAssignmentRequest {

    @NotNull(message = "Worker ID is required.")
    private Long workerId;

    @NotNull(message = "Assigned quantity is required.")
    @Positive(message = "Assigned quantity must be greater than zero.")
    private BigDecimal assignedQuantity;

    private String notes;
}