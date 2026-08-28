package com.hisabkitab.backend.order.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemMeasurementResponse {

    private Long measurementId;

    private String measurementName;

    private String unit;

    private BigDecimal value;
}