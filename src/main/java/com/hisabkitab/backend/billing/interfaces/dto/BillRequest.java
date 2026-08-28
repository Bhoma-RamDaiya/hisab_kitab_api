package com.hisabkitab.backend.billing.interfaces.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillRequest {

    /**
     * Completed orders that should be included in this bill.
     */
    @NotEmpty(message = "At least one order is required to create a bill.")
    private List<Long> orderIds;

    private String notes;
}