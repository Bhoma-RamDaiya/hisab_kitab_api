package com.hisabkitab.backend.product.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMeasurementRequest {

    @NotBlank(message = "Measurement name is required.")
    @Size(max = 100, message = "Measurement name cannot exceed 100 characters.")
    private String name;

    @Size(max = 50, message = "Unit cannot exceed 50 characters.")
    private String unit;

    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    private String description;
}
