package com.hisabkitab.backend.organization.interfaces.rest;

import com.hisabkitab.backend.organization.application.OrganizationSettingsService;
import com.hisabkitab.backend.organization.interfaces.dto.OrganizationSettingsRequest;
import com.hisabkitab.backend.organization.interfaces.dto.OrganizationSettingsResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations/{organizationId}/settings")
@RequiredArgsConstructor
public class OrganizationSettingsController {

    private final OrganizationSettingsService
            organizationSettingsService;

    @GetMapping
    public ResponseEntity<
            ApiResponse<OrganizationSettingsResponse>>
    getSettings(
            @PathVariable Long organizationId) {

        return ResponseEntity.ok(
                organizationSettingsService.getSettings(
                        organizationId
                )
        );
    }

    @PutMapping
    public ResponseEntity<
            ApiResponse<OrganizationSettingsResponse>>
    updateSettings(
            @PathVariable Long organizationId,
            @Valid @RequestBody
            OrganizationSettingsRequest request) {

        return ResponseEntity.ok(
                organizationSettingsService.updateSettings(
                        organizationId,
                        request
                )
        );
    }
}