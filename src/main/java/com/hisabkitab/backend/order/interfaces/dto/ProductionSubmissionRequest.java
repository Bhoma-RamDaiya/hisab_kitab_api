package com.hisabkitab.backend.order.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionSubmissionRequest {

    @NotNull(message = "Submitted quantity is required.")
    @Positive(message = "Submitted quantity must be greater than zero.")
    private BigDecimal submittedQuantity;

    private String submissionNotes;
}