package com.hisabkitab.backend.organization.interfaces.rest;

import com.hisabkitab.backend.organization.interfaces.dto.OrganizationResponse;
import com.hisabkitab.backend.organization.interfaces.dto.UpdateOrganizationRequest;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.organization.application.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/organization")
public class OrganizationController {
    private final OrganizationService organizationService;

    @GetMapping("/{organizationId}")
    public ApiResponse<OrganizationResponse> getOrganization(
            @PathVariable Long organizationId) {

        return organizationService.getOrganization(organizationId);
    }
    //Get All organization for a specific user
    @GetMapping("/allOrganizations")
    public ApiResponse<List<OrganizationResponse>> getOrganizations() {
        return organizationService.getAllOrganizations();
    }
    @PutMapping("/{organizationId}")
    public ApiResponse<String> updateOrganization(
            @PathVariable Long organizationId,
            @RequestBody UpdateOrganizationRequest request) {

        return organizationService.updateOrganization(organizationId, request);
    }



}
