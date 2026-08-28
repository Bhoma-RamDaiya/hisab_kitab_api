package com.hisabkitab.backend.billing.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItemResponse {

    private Long id;

    private Long orderId;

    private String buyerName;

    private String itemName;

    private BigDecimal quantity;

    private BigDecimal rate;

    private BigDecimal amount;
}