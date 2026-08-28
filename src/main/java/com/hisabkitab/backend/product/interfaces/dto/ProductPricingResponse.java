package com.hisabkitab.backend.product.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPricingResponse {

    private Long id;

    private Long organizationId;

    private Long productId;

    private String productName;

    private BigDecimal customerRate;

    private BigDecimal workerRate;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}