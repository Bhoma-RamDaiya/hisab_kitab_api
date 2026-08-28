package com.hisabkitab.backend.order.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;

    private Long productId;
    private String productName;

    private BigDecimal quantity;

    private BigDecimal rate;

    private BigDecimal amount;

    private String notes;
    private List<OrderItemMeasurementResponse> measurements;
}