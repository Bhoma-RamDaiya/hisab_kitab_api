package com.hisabkitab.backend.product.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMeasurementRequest {

    @NotNull(message = "Measurement ID is required.")
    private Long measurementId;

    @Builder.Default
    private Boolean required = true;

    private Integer displayOrder;
}