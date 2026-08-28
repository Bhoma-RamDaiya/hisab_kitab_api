package com.hisabkitab.backend.organization.application;
import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import com.hisabkitab.backend.organization.interfaces.dto.RegistrationStatus;
import com.hisabkitab.backend.user.interfaces.Status;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.organization.interfaces.dto.OrganizationRegistrationRequestDto;
import com.hisabkitab.backend.organization.interfaces.dto.OrganizationRegistrationResponse;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.organization.domain.OrganizationRegistrationEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.organization.infrastructure.OrganizationRegistrationRepository;
import com.hisabkitab.backend.organization.infrastructure.OrganizationRepository;
import com.hisabkitab.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Service
@RequiredArgsConstructor
public class OrganizationRegistrationService {
    @Autowired
    OrganizationRegistrationRepository organizationRegistrationRepository;
    private final SecurityUtils securityUtils;
    private  final OrganizationRepository orgRepository;
    private  final OrganizationMemberRepository organizationMemberRepository;
    public ApiResponse<String> registerOrganization(OrganizationRegistrationRequestDto request) {
        UserEntity applicant = securityUtils.getCurrentUser();
        OrganizationRegistrationEntity entity =
                OrganizationRegistrationEntity.builder()
                        .applicant(applicant)
                        .organizationName(request.getOrganizationName())
                        .ownerName(request.getOwnerName())
                        .panNumber(request.getPanNumber())
                        .gstNumber(request.getGstNumber())
                        .aadhaarNumber(request.getAadhaarNumber())
                        .mobileNumber(request.getMobileNumber())
                        .address(request.getAddress())
                        .status(RegistrationStatus.PENDING)
                        .build();

        organizationRegistrationRepository.save(entity);
        return  ApiResponse.<String>builder()
                .success(false)
                .message("Your are registered successfully wait for verification")
                .data("Success : your request has been sent for approval")
                .build();

    }

// all registration Request sending

     public ApiResponse<List<OrganizationRegistrationResponse>> getAllRegisterOrg(){

        List<OrganizationRegistrationEntity> orgList = organizationRegistrationRepository.findAll();
         List<OrganizationRegistrationResponse> responseList =
                 orgList.stream()
                         .map(org -> OrganizationRegistrationResponse.builder()
                                 .registrationId(org.getId())
                                 .organizationName(org.getOrganizationName())
                                 .ownerName(org.getOwnerName())
                                 .mobileNumber(org.getMobileNumber())
                                 .status(org.getStatus())
                                 .createdAt(org.getCreatedAt())
                                 .build())
                         .toList();

         return ApiResponse.<List<OrganizationRegistrationResponse>>builder()
                 .success(true)
                 .message("Organization registrations fetched successfully")
                 .data(responseList)
                 .build();
     }

    @Transactional
    public ApiResponse<String> approveRegisterOrg(Long registrationId, boolean approved) {

        OrganizationRegistrationEntity registration = organizationRegistrationRepository
                .findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Organization registration not found."));

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new RuntimeException("Registration has already been processed.");
        }

        // Logged in Platform Super Admin
        UserEntity superAdmin = securityUtils.getCurrentUser();

        if (approved) {

            if (orgRepository.existsByPanNumber(registration.getPanNumber())) {
                throw new RuntimeException("Organization already exists with this PAN number.");
            }

            OrganizationEntity organization = OrganizationEntity.builder()
                    .organizationName(registration.getOrganizationName())
                    .organizationCode(generateOrganizationCode())
                    .address(registration.getAddress())
                    .phoneNumber(registration.getMobileNumber())
                    .gstNumber(registration.getGstNumber())
                    .panNumber(registration.getPanNumber())
                    .status(Status.ACTIVE)

                    // Owner of Organization
                    .createdBy(registration.getApplicant())

                    .build();

            organization = orgRepository.save(organization);

            OrganizationMemberEntity ownerMember = OrganizationMemberEntity.builder()
                    .organization(organization)
                    .user(registration.getApplicant())
                    .role(OrganizationRole.OWNER)
                    .status(MembershipStatus.ACTIVE)

                    // Record who inserted this membership
                    .createdBy(superAdmin)

                    .joinedAt(LocalDateTime.now())
                    .build();

            organizationMemberRepository.save(ownerMember);

            registration.setStatus(RegistrationStatus.APPROVED);
            registration.setApprovedBy(superAdmin);
            registration.setApprovedAt(LocalDateTime.now());
            registration.setAdminRemark("Organization approved successfully.");

        } else {

            registration.setStatus(RegistrationStatus.REJECTED);
            registration.setApprovedBy(superAdmin);
            registration.setApprovedAt(LocalDateTime.now());
            registration.setAdminRemark("Organization rejected.");
        }

        organizationRegistrationRepository.save(registration);

        return ApiResponse.<String>builder()
                .success(true)
                .message(approved
                        ? "Organization approved successfully."
                        : "Organization rejected successfully.")
                .data(approved
                        ? "Organization approved successfully."
                        : "Organization rejected successfully.")
                .build();
    }
    private String generateOrganizationCode() {

        String code;

        do {
            code = "ORG" + String.format("%06d",
                    ThreadLocalRandom.current().nextInt(1, 999999));
        } while (orgRepository.existsByOrganizationCode(code));

        return code;
    }
}
