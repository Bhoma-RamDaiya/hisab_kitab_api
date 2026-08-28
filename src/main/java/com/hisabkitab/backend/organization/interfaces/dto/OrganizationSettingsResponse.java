package com.hisabkitab.backend.organization.interfaces.dto;

import com.hisabkitab.backend.billing.domain.PaymentAllocationMethod;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationSettingsResponse {

    private Long organizationId;

    private PaymentAllocationMethod paymentAllocationMethod;
}