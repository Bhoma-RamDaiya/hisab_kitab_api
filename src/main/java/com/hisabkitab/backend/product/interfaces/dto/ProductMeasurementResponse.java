package com.hisabkitab.backend.product.interfaces.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMeasurementResponse {

    private Long id;

    private Long measurementId;

    private String measurementName;

    private String unit;

    private Boolean required;

    private Integer displayOrder;
}