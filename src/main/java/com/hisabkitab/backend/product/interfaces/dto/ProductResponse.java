package com.hisabkitab.backend.product.interfaces.dto;

import com.hisabkitab.backend.product.interfaces.dto.ProductMeasurementResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;

    private Long organizationId;

    private String name;

    private String description;

    private Boolean active;

    private List<ProductMeasurementResponse> measurements;
}