package com.hisabkitab.backend.controller;

import com.hisabkitab.backend.dto.common.ApiResponse;
import com.hisabkitab.backend.dto.request.OrganizationRegistrationRequestDto;
import com.hisabkitab.backend.dto.response.OrganizationRegistrationResponse;
import com.hisabkitab.backend.services.OrganizationRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization")
public class OrganizationRegistrationController {
    @Autowired
    private OrganizationRegistrationService organizationService;

    @PostMapping("/organization_registration")
    public ResponseEntity<ApiResponse<String>> registerOrg(@RequestBody OrganizationRegistrationRequestDto requestDto) {
       ApiResponse<String> response = organizationService.registerOrganization(requestDto);
        return  ResponseEntity.ok(response);
 }
    @GetMapping("/get_organization_registration")
    public ResponseEntity<ApiResponse<List<OrganizationRegistrationResponse>>> allRegisterOrg() {
        ApiResponse<List<OrganizationRegistrationResponse>> response = organizationService.getAllRegisterOrg();
        return  ResponseEntity.ok(response);
    }
    @PutMapping("/approve_registered_org")
    public ResponseEntity<ApiResponse<String>> approveRegisterOrg(@RequestParam Long orgId) {
        ApiResponse<String> response = organizationService.approveRegisterOrg(orgId , true);
        return  ResponseEntity.ok(response);
    }
    @PutMapping("/reject_registered_org")
    public ResponseEntity<ApiResponse<String>> rejectRegisterOrg(@RequestParam Long orgId) {
        ApiResponse<String> response = organizationService.approveRegisterOrg(orgId , false);
        return  ResponseEntity.ok(response);
    }
 //POST   /organization-registrations          // Submit registration
    //GET    /organization-registrations          // List registrations
    //GET    /organization-registrations/{id}     // Registration details
    //PUT    /organization-registrations/{id}/approve
    //PUT    /organization-registrations/{id}/reject
}
