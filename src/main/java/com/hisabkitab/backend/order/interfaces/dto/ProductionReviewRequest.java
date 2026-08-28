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
public class ProductionReviewRequest {

    @NotNull(message = "Accepted quantity is required.")
    @PositiveOrZero(
            message = "Accepted quantity cannot be negative."
    )
    private BigDecimal acceptedQuantity;

    @NotNull(message = "Rejected quantity is required.")
    @PositiveOrZero(
            message = "Rejected quantity cannot be negative."
    )
    private BigDecimal rejectedQuantity;

    private String reviewNotes;
}