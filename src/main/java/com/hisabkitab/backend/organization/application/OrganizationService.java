package com.hisabkitab.backend.organization.application;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.organization.interfaces.dto.UpdateOrganizationRequest;
import com.hisabkitab.backend.organization.interfaces.dto.OrganizationResponse;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.organization.infrastructure.OrganizationRepository;
import com.hisabkitab.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final SecurityUtils securityUtils;
    private  final OrganizationRepository orgRepository;
    private  final OrganizationMemberRepository organizationMemberRepository;
    private  final OrganizationRepository organizationRepository;
    public ApiResponse<OrganizationResponse> getOrganization(Long organizationId) {

        UserEntity currentUser = securityUtils.getCurrentUser();

        organizationMemberRepository
                .findByOrganizationIdAndUserIdAndStatus(
                        organizationId,
                        currentUser.getId(),
                        MembershipStatus.ACTIVE)
                .orElseThrow(() ->
                        new RuntimeException("You are not a member of this organization."));

        Optional<OrganizationEntity> organizationOptional =
                organizationRepository.findById(organizationId);

        if (organizationOptional.isEmpty()) {

            return ApiResponse.<OrganizationResponse>builder()
                    .success(true)
                    .message("Organization not found.")
                    .data(null)
                    .build();
        }

        OrganizationEntity organization = organizationOptional.get();

        OrganizationResponse response = OrganizationResponse.builder()
                .id(organization.getId())
                .organizationName(organization.getOrganizationName())
                .organizationCode(organization.getOrganizationCode())
                .address(organization.getAddress())
                .phoneNumber(organization.getPhoneNumber())
                .gstNumber(organization.getGstNumber())
                .panNumber(organization.getPanNumber())
                .status(organization.getStatus())
                .build();

        return ApiResponse.<OrganizationResponse>builder()
                .success(true)
                .message("Organization fetched successfully.")
                .data(response)
                .build();
    }



    @Transactional
    public ApiResponse<String> updateOrganization(
            Long organizationId,
            UpdateOrganizationRequest request) {

        UserEntity currentUser = securityUtils.getCurrentUser();

        OrganizationMemberEntity member =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                currentUser.getId(),
                                MembershipStatus.ACTIVE)
                        .orElseThrow(() ->
                                new RuntimeException("You are not a member of this organization."));

        if (member.getRole() != OrganizationRole.OWNER) {
            throw new RuntimeException("Only organization owner can update organization.");
        }

        OrganizationEntity organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() ->
                        new RuntimeException("Organization not found."));

        organization.setOrganizationName(request.getOrganizationName());
        organization.setAddress(request.getAddress());
        organization.setPhoneNumber(request.getPhoneNumber());
        organization.setGstNumber(request.getGstNumber());

        organizationRepository.save(organization);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Organization updated successfully.")
                .data("Organization updated successfully.")
                .build();
    }




    public ApiResponse<List<OrganizationResponse>> getAllOrganizations() {

        UserEntity currentUser = securityUtils.getCurrentUser();

        List<OrganizationMemberEntity> organizationMembers =
                organizationMemberRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(
                        currentUser.getId(),
                        MembershipStatus.ACTIVE
                );

        List<OrganizationResponse> organizations = organizationMembers.stream()
                .map(member -> {

                    OrganizationEntity organization = member.getOrganization();

                    return OrganizationResponse.builder()
                            .id(organization.getId())
                            .organizationName(organization.getOrganizationName())
                            .organizationCode(organization.getOrganizationCode())
                            .address(organization.getAddress())
                            .phoneNumber(organization.getPhoneNumber())
                            .gstNumber(organization.getGstNumber())
                            .panNumber(organization.getPanNumber())
                            .status(organization.getStatus())
                            .build();
                })
                .toList();

        return ApiResponse.<List<OrganizationResponse>>builder()
                .success(true)
                .message("Organizations fetched successfully.")
                .data(organizations)
                .build();
    }
}
