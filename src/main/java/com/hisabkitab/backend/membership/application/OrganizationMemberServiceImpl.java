package com.hisabkitab.backend.membership.application;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.membership.interfaces.OrganizationMemberResponse;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrganizationMemberServiceImpl
        implements OrganizationMemberService {

    private final SecurityUtils securityUtils;

    private final OrganizationMemberRepository
            organizationMemberRepository;


    // =========================================================
    // GET MEMBERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrganizationMemberResponse>> getMembers(
            Long organizationId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();

        // =========================================================
        // CHECK CURRENT USER MEMBERSHIP
        // =========================================================

        OrganizationMemberEntity currentMember =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                currentUser.getId(),
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not an active member of this organization."
                                )
                        );


        // =========================================================
        // CHECK ROLE
        // =========================================================
        //
        // Only these organization roles can view
        // the complete organization member list:
        //
        // OWNER
        // ADMIN
        // MANAGER
        // ACCOUNTANT
        //
        // WORKER and HELPER cannot access this endpoint.
        //

        OrganizationRole role =
                currentMember.getRole();

        if (role != OrganizationRole.OWNER
                && role != OrganizationRole.ADMIN
                && role != OrganizationRole.MANAGER
                && role != OrganizationRole.ACCOUNTANT) {

            throw new RuntimeException(
                    "You are not authorized to view organization members."
            );
        }


        // =========================================================
        // FETCH MEMBERS
        // =========================================================

        List<OrganizationMemberResponse> members =
                organizationMemberRepository
                        .findAllByOrganizationId(organizationId)
                        .stream()
                        .map(this::toResponse)
                        .toList();


        // =========================================================
        // RESPONSE
        // =========================================================

        return ApiResponse
                .<List<OrganizationMemberResponse>>builder()
                .success(true)
                .message(
                        "Organization members fetched successfully."
                )
                .data(members)
                .build();
    }


    // =========================================================
    // BLOCK MEMBER
    // =========================================================

    @Override
    @Transactional
    public ApiResponse<String> blockMember(
            Long organizationId,
            Long userId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        OrganizationMemberEntity owner =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                currentUser.getId(),
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this organization."
                                )
                        );


        if (owner.getRole() != OrganizationRole.OWNER) {

            throw new RuntimeException(
                    "Only owner can block members."
            );
        }


        OrganizationMemberEntity member =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                userId,
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Active member not found."
                                )
                        );


        if (member.getRole() == OrganizationRole.OWNER) {

            throw new RuntimeException(
                    "Owner cannot be blocked."
            );
        }


        member.setStatus(
                MembershipStatus.BLOCKED
        );


        organizationMemberRepository.save(member);


        return ApiResponse
                .<String>builder()
                .success(true)
                .message("Member blocked successfully.")
                .data("Member blocked successfully.")
                .build();
    }


    // =========================================================
    // UNBLOCK MEMBER
    // =========================================================

    @Override
    @Transactional
    public ApiResponse<String> unblockMember(
            Long organizationId,
            Long userId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        OrganizationMemberEntity owner =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                currentUser.getId(),
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this organization."
                                )
                        );


        if (owner.getRole() != OrganizationRole.OWNER) {

            throw new RuntimeException(
                    "Only owner can unblock members."
            );
        }


        OrganizationMemberEntity member =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                userId,
                                MembershipStatus.BLOCKED
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Blocked member not found."
                                )
                        );


        member.setStatus(
                MembershipStatus.ACTIVE
        );


        organizationMemberRepository.save(member);


        return ApiResponse
                .<String>builder()
                .success(true)
                .message("Member unblocked successfully.")
                .data("Member unblocked successfully.")
                .build();
    }


    // =========================================================
    // REMOVE MEMBER
    // =========================================================

    @Override
    @Transactional
    public ApiResponse<String> removeMember(
            Long organizationId,
            Long userId,
            String request) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        OrganizationMemberEntity owner =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                currentUser.getId(),
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not a member of this organization."
                                )
                        );


        if (owner.getRole() != OrganizationRole.OWNER) {

            throw new RuntimeException(
                    "Only owner can remove members."
            );
        }


        OrganizationMemberEntity member =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                userId,
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Active member not found."
                                )
                        );


        if (member.getRole() == OrganizationRole.OWNER) {

            throw new RuntimeException(
                    "Owner cannot be removed."
            );
        }


        member.setStatus(
                MembershipStatus.REMOVED
        );

        member.setLeftAt(
                LocalDateTime.now()
        );

        member.setExitRemark(request);


        organizationMemberRepository.save(member);


        return ApiResponse
                .<String>builder()
                .success(true)
                .message("Member removed successfully.")
                .data("Member removed successfully.")
                .build();
    }


    // =========================================================
    // LEAVE ORGANIZATION
    // =========================================================

    @Override
    @Transactional
    public ApiResponse<String> leaveOrganization(
            Long organizationId,
            String request) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        OrganizationMemberEntity member =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                currentUser.getId(),
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Active membership not found."
                                )
                        );


        if (member.getRole() == OrganizationRole.OWNER) {

            throw new RuntimeException(
                    "Owner cannot leave the organization."
            );
        }


        member.setStatus(
                MembershipStatus.LEFT
        );

        member.setLeftAt(
                LocalDateTime.now()
        );

        member.setExitRemark(request);


        organizationMemberRepository.save(member);


        return ApiResponse
                .<String>builder()
                .success(true)
                .message(
                        "You left the organization successfully."
                )
                .data(
                        "You left the organization successfully."
                )
                .build();
    }


    // =========================================================
    // RESPONSE MAPPER
    // =========================================================

    private OrganizationMemberResponse toResponse(
            OrganizationMemberEntity member) {

        UserEntity user =
                member.getUser();


        return OrganizationMemberResponse
                .builder()
                .userId(
                        user != null
                                ? user.getId()
                                : null
                )
                .name(
                        user != null
                                ? user.getName()
                                : member.getMemberOrganization()
                                .getOrganizationName()
                )
                .username(
                        user != null
                                ? user.getUsername()
                                : null
                )
                .role(
                        member.getRole()
                )
                .status(
                        member.getStatus()
                )
                .joinedAt(
                        member.getJoinedAt()
                )
                .build();
    }
}