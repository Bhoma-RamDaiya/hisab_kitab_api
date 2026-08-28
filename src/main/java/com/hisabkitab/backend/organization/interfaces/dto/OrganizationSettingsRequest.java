package com.hisabkitab.backend.organization.interfaces.dto;

import com.hisabkitab.backend.billing.domain.PaymentAllocationMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationSettingsRequest {

    @NotNull(message = "Payment allocation method is required.")
    private PaymentAllocationMethod paymentAllocationMethod;
}