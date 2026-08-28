package com.hisabkitab.backend.order.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @NotNull(message = "Product ID is required.")
    private Long productId;

    @NotNull(message = "Quantity is required.")
    @Positive(message = "Quantity must be greater than zero.")
    private BigDecimal quantity;

    @NotNull(message = "Rate is required.")
    @Positive(message = "Rate must be greater than zero.")
    private BigDecimal rate;

    private String notes;

    @Valid
    @Builder.Default
    private List<OrderItemMeasurementRequest> measurements =
            new ArrayList<>();
}