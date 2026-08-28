package com.hisabkitab.backend.user.interfaces;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.organization.interfaces.dto.OrganizationRegistrationResponse;
import com.hisabkitab.backend.organization.application.OrganizationRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {
    private  final OrganizationRegistrationService registrationService;
    @GetMapping("/organization-registrations")
    public ResponseEntity<ApiResponse<List<OrganizationRegistrationResponse>>> getAllOrganisationRegistration(){
        ApiResponse<List<OrganizationRegistrationResponse>> response = registrationService.getAllRegisterOrg();
        return ResponseEntity.ok(response);

    }

    @GetMapping("/organization-registrations/hee")
    public void  getAllOrganisationById(@RequestParam Long id){}
    @GetMapping("/organization-registrations/tee")
    public void  updateOrganisationByIdAndStatus(@RequestParam Long id){}
}
