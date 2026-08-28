package com.hisabkitab.backend.product.interfaces.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeasurementResponse {

    private Long id;

    private Long organizationId;

    private String name;

    private String unit;

    private String description;

    private Boolean active;
}
