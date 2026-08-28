package com.hisabkitab.backend.organization.application;

import com.hisabkitab.backend.organization.interfaces.dto.OrganizationSettingsRequest;
import com.hisabkitab.backend.organization.interfaces.dto.OrganizationSettingsResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;

public interface OrganizationSettingsService {

    ApiResponse<OrganizationSettingsResponse> getSettings(
            Long organizationId
    );

    ApiResponse<OrganizationSettingsResponse> updateSettings(
            Long organizationId,
            OrganizationSettingsRequest request
    );
}