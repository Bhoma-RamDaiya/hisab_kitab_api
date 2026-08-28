package com.hisabkitab.backend.organization.application;

import com.hisabkitab.backend.billing.domain.PaymentAllocationMethod;
import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import com.hisabkitab.backend.organization.infrastructure.OrganizationRepository;
import com.hisabkitab.backend.organization.interfaces.dto.OrganizationSettingsRequest;
import com.hisabkitab.backend.organization.interfaces.dto.OrganizationSettingsResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationSettingsServiceImpl
        implements OrganizationSettingsService {

    private final OrganizationRepository organizationRepository;

    private final OrganizationMemberRepository
            organizationMemberRepository;

    private final SecurityUtils securityUtils;


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrganizationSettingsResponse>
    getSettings(Long organizationId) {

        /*
         * Logged-in user must be an active member
         * of this organization.
         */
        getCurrentOrganizationMember(
                organizationId
        );


        /*
         * Organization must exist.
         */
        OrganizationEntity organization =
                organizationRepository
                        .findById(organizationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Organization not found."
                                ));


        /*
         * Safety fallback for organizations created
         * before payment allocation setting existed.
         */
        PaymentAllocationMethod method =
                organization.getPaymentAllocationMethod();

        if (method == null) {
            method =
                    PaymentAllocationMethod
                            .LATEST_BILL_FIRST;
        }


        OrganizationSettingsResponse response =
                OrganizationSettingsResponse.builder()
                        .organizationId(
                                organization.getId()
                        )
                        .paymentAllocationMethod(
                                method
                        )
                        .build();


        return ApiResponse
                .<OrganizationSettingsResponse>builder()
                .success(true)
                .message(
                        "Organization settings fetched successfully."
                )
                .data(response)
                .build();
    }


    @Override
    @Transactional
    public ApiResponse<OrganizationSettingsResponse>
    updateSettings(
            Long organizationId,
            OrganizationSettingsRequest request) {

        /*
         * Only an authorized organization member
         * can update settings.
         */
        OrganizationMemberEntity currentMember =
                getCurrentOrganizationMember(
                        organizationId
                );


        /*
         * Only organization owner can change
         * payment allocation settings.
         */
        if (currentMember.getRole() != OrganizationRole.OWNER) {

            throw new RuntimeException(
                    "Only the organization owner can update settings."
            );
        }


        /*
         * Organization must exist.
         */
        OrganizationEntity organization =
                organizationRepository
                        .findById(organizationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Organization not found."
                                ));


        /*
         * Request validation is normally handled
         * by @Valid, but keep a service-level check.
         */
        if (request == null ||
                request.getPaymentAllocationMethod() == null) {

            throw new RuntimeException(
                    "Payment allocation method is required."
            );
        }


        /*
         * Update organization setting.
         */
        organization.setPaymentAllocationMethod(
                request.getPaymentAllocationMethod()
        );


        organization =
                organizationRepository.save(
                        organization
                );


        OrganizationSettingsResponse response =
                OrganizationSettingsResponse.builder()
                        .organizationId(
                                organization.getId()
                        )
                        .paymentAllocationMethod(
                                organization
                                        .getPaymentAllocationMethod()
                        )
                        .build();


        return ApiResponse
                .<OrganizationSettingsResponse>builder()
                .success(true)
                .message(
                        "Organization settings updated successfully."
                )
                .data(response)
                .build();
    }


    /*
     * Returns the current user's active membership
     * in the requested organization.
     */
    private OrganizationMemberEntity
    getCurrentOrganizationMember(
            Long organizationId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        return organizationMemberRepository
                .findByOrganizationIdAndUserIdAndStatus(
                        organizationId,
                        currentUser.getId(),
                        MembershipStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not an active member "
                                        + "of this organization."
                        ));
    }
}