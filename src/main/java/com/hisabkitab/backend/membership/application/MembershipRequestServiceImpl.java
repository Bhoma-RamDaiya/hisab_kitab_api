package com.hisabkitab.backend.membership.application;

import com.hisabkitab.backend.membership.domain.MembershipRequestEntity;
import com.hisabkitab.backend.membership.domain.MembershipRequestStatus;
import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.MembershipRequestRepository;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.membership.interfaces.MembershipRequestRequest;
import com.hisabkitab.backend.membership.interfaces.MembershipRequestResponse;
import com.hisabkitab.backend.membership.interfaces.RequestType;
import com.hisabkitab.backend.notification.application.NotificationService;
import com.hisabkitab.backend.notification.domain.NotificationType;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import com.hisabkitab.backend.organization.infrastructure.OrganizationRepository;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.user.infrastructure.UserRepository;
import com.hisabkitab.backend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipRequestServiceImpl
        implements MembershipRequestService {

    private final SecurityUtils securityUtils;

    private final MembershipRequestRepository membershipRequestRepository;

    private final OrganizationMemberRepository organizationMemberRepository;

    private final OrganizationRepository organizationRepository;

    private final UserRepository userRepository;

    private final NotificationService notificationService;


    // ============================================================
    // SEND REQUEST
    // ============================================================

    @Override
    @Transactional
    public ApiResponse<String> sendMembershipRequest(
            MembershipRequestRequest request) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        if (request == null) {
            throw new RuntimeException(
                    "Membership request is required."
            );
        }


        if (request.getRequestType() == null) {
            throw new RuntimeException(
                    "Request type is required."
            );
        }


        switch (request.getRequestType()) {

            // ====================================================
            // JOIN REQUEST
            // ====================================================

            case JOIN_REQUEST -> {

                if (request.getOrganizationId() == null) {
                    throw new RuntimeException(
                            "Organization Id is required."
                    );
                }


                OrganizationEntity organization =
                        organizationRepository
                                .findById(
                                        request.getOrganizationId()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Organization not found."
                                        )
                                );


                /*
                 * ------------------------------------------------
                 * USER -> ORGANIZATION
                 * ------------------------------------------------
                 *
                 * If memberOrganizationId is not supplied,
                 * current logged-in user is requesting to join.
                 */
                if (request.getMemberOrganizationId() == null) {

                    /*
                     * A user cannot join an organization
                     * where he is already an active member.
                     */
                    boolean alreadyMember =
                            organizationMemberRepository
                                    .findByOrganizationIdAndUserIdAndStatus(
                                            organization.getId(),
                                            currentUser.getId(),
                                            MembershipStatus.ACTIVE
                                    )
                                    .isPresent();


                    if (alreadyMember) {
                        throw new RuntimeException(
                                "You are already an active member "
                                        + "of this organization."
                        );
                    }


                    /*
                     * Prevent duplicate pending request.
                     */
                    boolean alreadyRequested =
                            membershipRequestRepository
                                    .existsByOrganizationIdAndUserIdAndStatus(
                                            organization.getId(),
                                            currentUser.getId(),
                                            MembershipRequestStatus.PENDING
                                    );


                    if (alreadyRequested) {
                        throw new RuntimeException(
                                "You already have a pending "
                                        + "request for this organization."
                        );
                    }


                    MembershipRequestEntity membershipRequest =
                            MembershipRequestEntity.builder()
                                    .organization(organization)
                                    .user(currentUser)
                                    .memberOrganization(null)
                                    .requestType(
                                            RequestType.JOIN_REQUEST
                                    )
                                    .status(
                                            MembershipRequestStatus.PENDING
                                    )
                                    .build();


                    membershipRequestRepository.save(
                            membershipRequest
                    );


                    return ApiResponse
                            .<String>builder()
                            .success(true)
                            .message(
                                    "Join request sent successfully."
                            )
                            .data(
                                    "Your request has been sent "
                                            + "to the organization."
                            )
                            .build();
                }


                // =================================================
                // ORGANIZATION -> ORGANIZATION JOIN REQUEST
                // =================================================

                OrganizationEntity memberOrganization =
                        organizationRepository
                                .findById(
                                        request.getMemberOrganizationId()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Member organization not found."
                                        )
                                );


                /*
                 * An organization cannot request to join itself.
                 */
                if (organization.getId()
                        .equals(memberOrganization.getId())) {

                    throw new RuntimeException(
                            "An organization cannot send a "
                                    + "membership request to itself."
                    );
                }


                /*
                 * Current user must be OWNER of the
                 * organization being represented.
                 */
                validateOrganizationOwner(
                        memberOrganization.getId(),
                        currentUser
                );


                /*
                 * Organization must not already be a member.
                 */
                boolean alreadyMember =
                        organizationMemberRepository
                                .existsByOrganizationIdAndMemberOrganizationIdAndStatus(
                                        organization.getId(),
                                        memberOrganization.getId(),
                                        MembershipStatus.ACTIVE
                                );


                if (alreadyMember) {
                    throw new RuntimeException(
                            "This organization is already a "
                                    + "member of the target organization."
                    );
                }


                /*
                 * Prevent duplicate pending request.
                 */
                boolean alreadyRequested =
                        membershipRequestRepository
                                .existsByOrganizationIdAndMemberOrganizationIdAndStatus(
                                        organization.getId(),
                                        memberOrganization.getId(),
                                        MembershipRequestStatus.PENDING
                                );


                if (alreadyRequested) {
                    throw new RuntimeException(
                            "A pending organization membership "
                                    + "request already exists."
                    );
                }


                MembershipRequestEntity membershipRequest =
                        MembershipRequestEntity.builder()
                                .organization(organization)
                                .user(null)
                                .memberOrganization(
                                        memberOrganization
                                )
                                .requestType(
                                        RequestType.JOIN_REQUEST
                                )
                                .status(
                                        MembershipRequestStatus.PENDING
                                )
                                .build();


                membershipRequestRepository.save(
                        membershipRequest
                );


                /*
                 * Notify owner of the target organization.
                 */
                UserEntity targetOwner =
                        organization.getCreatedBy();


                if (targetOwner != null) {

                    notificationService.sendNotification(
                            targetOwner,
                            memberOrganization,
                            NotificationType.MEMBERSHIP_REQUEST,
                            "Organization Join Request",
                            memberOrganization.getOrganizationName()
                                    + " has requested to join "
                                    + organization.getOrganizationName()
                                    + " as a member organization.",
                            membershipRequest.getId()
                    );
                }


                return ApiResponse
                        .<String>builder()
                        .success(true)
                        .message(
                                "Organization join request sent successfully."
                        )
                        .data(
                                "Organization membership request "
                                        + "has been sent successfully."
                        )
                        .build();
            }


            // ====================================================
            // INVITATION
            // ====================================================

            case INVITATION -> {

                if (request.getOrganizationId() == null) {
                    throw new RuntimeException(
                            "Organization Id is required."
                    );
                }


                OrganizationEntity organization =
                        organizationRepository
                                .findById(
                                        request.getOrganizationId()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Organization not found."
                                        )
                                );


                /*
                 * Only OWNER can send invitations.
                 */
                validateOrganizationOwner(
                        organization.getId(),
                        currentUser
                );


                // =================================================
                // ORGANIZATION -> USER INVITATION
                // =================================================

                if (request.getUserId() != null
                        && request.getMemberOrganizationId() == null) {

                    UserEntity invitedUser =
                            userRepository
                                    .findById(
                                            request.getUserId()
                                    )
                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "User not found."
                                            )
                                    );


                    /*
                     * User cannot be invited if already
                     * an active member.
                     */
                    boolean alreadyMember =
                            organizationMemberRepository
                                    .findByOrganizationIdAndUserIdAndStatus(
                                            organization.getId(),
                                            invitedUser.getId(),
                                            MembershipStatus.ACTIVE
                                    )
                                    .isPresent();


                    if (alreadyMember) {
                        throw new RuntimeException(
                                "User is already a member "
                                        + "of this organization."
                        );
                    }


                    boolean alreadyRequested =
                            membershipRequestRepository
                                    .existsByOrganizationIdAndUserIdAndStatus(
                                            organization.getId(),
                                            invitedUser.getId(),
                                            MembershipRequestStatus.PENDING
                                    );


                    if (alreadyRequested) {
                        throw new RuntimeException(
                                "A pending invitation already exists "
                                        + "for this user."
                        );
                    }


                    MembershipRequestEntity membershipRequest =
                            MembershipRequestEntity.builder()
                                    .organization(organization)
                                    .user(invitedUser)
                                    .memberOrganization(null)
                                    .requestType(
                                            RequestType.INVITATION
                                    )
                                    .status(
                                            MembershipRequestStatus.PENDING
                                    )
                                    .build();


                    membershipRequestRepository.save(
                            membershipRequest
                    );


                    notificationService.sendNotification(
                            invitedUser,
                            organization,
                            NotificationType.ORGANIZATION_INVITATION,
                            "Organization Invitation",
                            organization.getOrganizationName()
                                    + " has invited you "
                                    + "to join their organization.",
                            membershipRequest.getId()
                    );


                    return ApiResponse
                            .<String>builder()
                            .success(true)
                            .message(
                                    "Invitation sent successfully."
                            )
                            .data(
                                    "Invitation sent successfully."
                            )
                            .build();
                }


                // =================================================
                // ORGANIZATION -> ORGANIZATION INVITATION
                // =================================================

                if (request.getUserId() == null
                        && request.getMemberOrganizationId() != null) {

                    OrganizationEntity invitedOrganization =
                            organizationRepository
                                    .findById(
                                            request.getMemberOrganizationId()
                                    )
                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "Invited organization not found."
                                            )
                                    );


                    /*
                     * Cannot invite itself.
                     */
                    if (organization.getId()
                            .equals(invitedOrganization.getId())) {

                        throw new RuntimeException(
                                "An organization cannot invite itself."
                        );
                    }


                    boolean alreadyMember =
                            organizationMemberRepository
                                    .existsByOrganizationIdAndMemberOrganizationIdAndStatus(
                                            organization.getId(),
                                            invitedOrganization.getId(),
                                            MembershipStatus.ACTIVE
                                    );


                    if (alreadyMember) {
                        throw new RuntimeException(
                                "This organization is already "
                                        + "a member of your organization."
                        );
                    }


                    boolean alreadyRequested =
                            membershipRequestRepository
                                    .existsByOrganizationIdAndMemberOrganizationIdAndStatus(
                                            organization.getId(),
                                            invitedOrganization.getId(),
                                            MembershipRequestStatus.PENDING
                                    );


                    if (alreadyRequested) {
                        throw new RuntimeException(
                                "A pending invitation already exists "
                                        + "for this organization."
                        );
                    }


                    MembershipRequestEntity membershipRequest =
                            MembershipRequestEntity.builder()
                                    .organization(organization)
                                    .user(null)
                                    .memberOrganization(
                                            invitedOrganization
                                    )
                                    .requestType(
                                            RequestType.INVITATION
                                    )
                                    .status(
                                            MembershipRequestStatus.PENDING
                                    )
                                    .build();


                    membershipRequestRepository.save(
                            membershipRequest
                    );


                    /*
                     * Notify owner of invited organization.
                     */
                    UserEntity invitedOrganizationOwner =
                            invitedOrganization.getCreatedBy();


                    if (invitedOrganizationOwner != null) {

                        notificationService.sendNotification(
                                invitedOrganizationOwner,
                                organization,
                                NotificationType.ORGANIZATION_INVITATION,
                                "Organization Invitation",
                                organization.getOrganizationName()
                                        + " has invited "
                                        + invitedOrganization.getOrganizationName()
                                        + " to join their organization.",
                                membershipRequest.getId()
                        );
                    }


                    return ApiResponse
                            .<String>builder()
                            .success(true)
                            .message(
                                    "Organization invitation sent successfully."
                            )
                            .data(
                                    "Organization invitation sent successfully."
                            )
                            .build();
                }


                throw new RuntimeException(
                        "Either userId or memberOrganizationId "
                                + "must be provided for an invitation."
                );
            }


            default -> throw new RuntimeException(
                    "Invalid request type."
            );
        }
    }


    // ============================================================
    // APPROVE
    // ============================================================

    @Override
    @Transactional
    public ApiResponse<String> approveMembershipRequest(
            Long requestId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();

        MembershipRequestEntity request =
                getPendingRequest(requestId);


        // ============================================================
        // JOIN REQUEST
        // ============================================================
        //
        // JOIN_REQUEST means:
        //
        // User/Organization
        //        ↓
        //   wants to join
        //        ↓
        // Target Organization
        //
        // Therefore the OWNER of the target organization
        // must approve it.
        //
        if (request.getRequestType()
                == RequestType.JOIN_REQUEST) {

            validateOrganizationOwner(
                    request.getOrganization().getId(),
                    currentUser
            );


            // =========================================================
            // USER -> ORGANIZATION
            // =========================================================

            if (request.getUser() != null) {

                UserEntity user =
                        request.getUser();


                /*
                 * Check only ACTIVE membership.
                 *
                 * A previous LEFT / REMOVED membership
                 * must not prevent the user from joining again.
                 */
                boolean alreadyMember =
                        organizationMemberRepository
                                .findByOrganizationIdAndUserIdAndStatus(
                                        request.getOrganization().getId(),
                                        user.getId(),
                                        MembershipStatus.ACTIVE
                                )
                                .isPresent();


                if (alreadyMember) {
                    throw new RuntimeException(
                            "User is already an active member "
                                    + "of this organization."
                    );
                }


                OrganizationMemberEntity member =
                        OrganizationMemberEntity.builder()
                                .organization(
                                        request.getOrganization()
                                )
                                .user(user)
                                .memberOrganization(null)
                                .role(OrganizationRole.WORKER)
                                .status(MembershipStatus.ACTIVE)
                                .createdBy(currentUser)
                                .joinedAt(LocalDateTime.now())
                                .build();


                organizationMemberRepository.save(member);


                request.setStatus(
                        MembershipRequestStatus.APPROVED
                );

                request.setProcessedBy(currentUser);

                request.setProcessedAt(
                        LocalDateTime.now()
                );


                membershipRequestRepository.save(request);


                notificationService.sendNotification(
                        user,
                        request.getOrganization(),
                        NotificationType.MEMBERSHIP_APPROVED,
                        "Membership Approved",
                        "Your request to join "
                                + request.getOrganization()
                                .getOrganizationName()
                                + " has been approved.",
                        request.getId()
                );


                return successResponse(
                        "Membership request approved successfully."
                );
            }


            // =========================================================
            // ORGANIZATION -> ORGANIZATION
            // =========================================================

            OrganizationEntity memberOrganization =
                    request.getMemberOrganization();


            if (memberOrganization == null) {

                throw new RuntimeException(
                        "Invalid membership request target."
                );
            }


            /*
             * Check only ACTIVE membership.
             */
            boolean alreadyMember =
                    organizationMemberRepository
                            .existsByOrganizationIdAndMemberOrganizationIdAndStatus(
                                    request.getOrganization().getId(),
                                    memberOrganization.getId(),
                                    MembershipStatus.ACTIVE
                            );


            if (alreadyMember) {
                throw new RuntimeException(
                        "Organization is already an active member."
                );
            }


            OrganizationMemberEntity member =
                    OrganizationMemberEntity.builder()
                            .organization(
                                    request.getOrganization()
                            )
                            .user(null)
                            .memberOrganization(
                                    memberOrganization
                            )
                            .role(OrganizationRole.WORKER)
                            .status(MembershipStatus.ACTIVE)
                            .createdBy(currentUser)
                            .joinedAt(LocalDateTime.now())
                            .build();


            organizationMemberRepository.save(member);


            request.setStatus(
                    MembershipRequestStatus.APPROVED
            );

            request.setProcessedBy(currentUser);

            request.setProcessedAt(
                    LocalDateTime.now()
            );


            membershipRequestRepository.save(request);


            /*
             * Notify the owner of the organization
             * that requested to join.
             */
            UserEntity memberOrganizationOwner =
                    memberOrganization.getCreatedBy();


            if (memberOrganizationOwner != null) {

                notificationService.sendNotification(
                        memberOrganizationOwner,
                        request.getOrganization(),
                        NotificationType.MEMBERSHIP_APPROVED,
                        "Organization Membership Approved",
                        memberOrganization.getOrganizationName()
                                + " has been approved as a member of "
                                + request.getOrganization()
                                .getOrganizationName()
                                + ".",
                        request.getId()
                );
            }


            return successResponse(
                    "Organization membership request approved successfully."
            );
        }


        // ============================================================
        // INVITATION
        // ============================================================
        //
        // INVITATION is different from JOIN_REQUEST.
        //
        // The organization that SENT the invitation does NOT approve it.
        //
        // The INVITED party accepts it:
        //
        // Organization -> User
        //       ↓
        //     User accepts
        //
        // Organization -> Organization
        //       ↓
        // Invited Organization OWNER accepts
        //
        if (request.getRequestType()
                == RequestType.INVITATION) {


            // =========================================================
            // ORGANIZATION -> USER INVITATION
            // =========================================================

            if (request.getUser() != null) {

                UserEntity invitedUser =
                        request.getUser();


                /*
                 * Only the invited user can accept
                 * their own invitation.
                 */
                if (!invitedUser.getId()
                        .equals(currentUser.getId())) {

                    throw new RuntimeException(
                            "Only the invited user can "
                                    + "accept this invitation."
                    );
                }


                /*
                 * Make sure the user isn't already
                 * an ACTIVE member.
                 */
                boolean alreadyMember =
                        organizationMemberRepository
                                .findByOrganizationIdAndUserIdAndStatus(
                                        request.getOrganization().getId(),
                                        invitedUser.getId(),
                                        MembershipStatus.ACTIVE
                                )
                                .isPresent();


                if (alreadyMember) {

                    throw new RuntimeException(
                            "User is already an active member "
                                    + "of this organization."
                    );
                }


                OrganizationMemberEntity member =
                        OrganizationMemberEntity.builder()
                                .organization(
                                        request.getOrganization()
                                )
                                .user(invitedUser)
                                .memberOrganization(null)
                                .role(OrganizationRole.WORKER)
                                .status(MembershipStatus.ACTIVE)
                                .createdBy(currentUser)
                                .joinedAt(LocalDateTime.now())
                                .build();


                organizationMemberRepository.save(member);


                request.setStatus(
                        MembershipRequestStatus.APPROVED
                );

                request.setProcessedBy(currentUser);

                request.setProcessedAt(
                        LocalDateTime.now()
                );


                membershipRequestRepository.save(request);


                /*
                 * Notify the organization owner
                 * that the user accepted.
                 */
                UserEntity organizationOwner =
                        request.getOrganization()
                                .getCreatedBy();


                if (organizationOwner != null) {

                    notificationService.sendNotification(
                            organizationOwner,
                            request.getOrganization(),
                            NotificationType.MEMBERSHIP_APPROVED,
                            "Invitation Accepted",
                            invitedUser.getName()
                                    + " has accepted your invitation "
                                    + "to join the organization.",
                            request.getId()
                    );
                }


                return successResponse(
                        "Invitation accepted successfully."
                );
            }


            // =========================================================
            // ORGANIZATION -> ORGANIZATION INVITATION
            // =========================================================

            OrganizationEntity invitedOrganization =
                    request.getMemberOrganization();


            if (invitedOrganization == null) {

                throw new RuntimeException(
                        "Invalid membership invitation target."
                );
            }


            /*
             * IMPORTANT:
             *
             * The INVITED organization accepts.
             *
             * Therefore the current user must be the OWNER
             * of the invited organization.
             */
            validateOrganizationOwner(
                    invitedOrganization.getId(),
                    currentUser
            );


            /*
             * Check only ACTIVE membership.
             */
            boolean alreadyMember =
                    organizationMemberRepository
                            .existsByOrganizationIdAndMemberOrganizationIdAndStatus(
                                    request.getOrganization().getId(),
                                    invitedOrganization.getId(),
                                    MembershipStatus.ACTIVE
                            );


            if (alreadyMember) {

                throw new RuntimeException(
                        "Organization is already an active member "
                                + "of this organization."
                );
            }


            OrganizationMemberEntity member =
                    OrganizationMemberEntity.builder()
                            .organization(
                                    request.getOrganization()
                            )
                            .user(null)
                            .memberOrganization(
                                    invitedOrganization
                            )
                            .role(OrganizationRole.WORKER)
                            .status(MembershipStatus.ACTIVE)
                            .createdBy(currentUser)
                            .joinedAt(LocalDateTime.now())
                            .build();


            organizationMemberRepository.save(member);


            request.setStatus(
                    MembershipRequestStatus.APPROVED
            );

            request.setProcessedBy(currentUser);

            request.setProcessedAt(
                    LocalDateTime.now()
            );


            membershipRequestRepository.save(request);


            /*
             * Notify the organization that sent
             * the invitation.
             */
            UserEntity inviterOwner =
                    request.getOrganization()
                            .getCreatedBy();


            if (inviterOwner != null) {

                notificationService.sendNotification(
                        inviterOwner,
                        request.getOrganization(),
                        NotificationType.MEMBERSHIP_APPROVED,
                        "Organization Invitation Accepted",
                        invitedOrganization.getOrganizationName()
                                + " has accepted your invitation "
                                + "to join "
                                + request.getOrganization()
                                .getOrganizationName()
                                + ".",
                        request.getId()
                );
            }


            return successResponse(
                    "Organization invitation accepted successfully."
            );
        }


        throw new RuntimeException(
                "Invalid membership request."
        );
    }


    // ============================================================
    // REJECT
    // ============================================================

    @Override
    @Transactional
    public ApiResponse<String> rejectMembershipRequest(
            Long requestId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        MembershipRequestEntity request =
                getPendingRequest(requestId);


        /*
         * JOIN_REQUEST:
         *
         * Target organization owner rejects.
         */
        if (request.getRequestType()
                == RequestType.JOIN_REQUEST) {

            validateOrganizationOwner(
                    request.getOrganization().getId(),
                    currentUser
            );


            request.setStatus(
                    MembershipRequestStatus.REJECTED
            );

            request.setProcessedBy(currentUser);

            request.setProcessedAt(
                    LocalDateTime.now()
            );


            membershipRequestRepository.save(request);


            /*
             * User join request.
             */
            if (request.getUser() != null) {

                notificationService.sendNotification(
                        request.getUser(),
                        request.getOrganization(),
                        NotificationType.MEMBERSHIP_REJECTED,
                        "Membership Request Rejected",
                        "Your request to join "
                                + request.getOrganization()
                                .getOrganizationName()
                                + " has been rejected.",
                        request.getId()
                );
            }

            /*
             * Organization join request.
             */
            else if (request.getMemberOrganization() != null) {

                UserEntity organizationOwner =
                        request.getMemberOrganization()
                                .getCreatedBy();


                if (organizationOwner != null) {

                    notificationService.sendNotification(
                            organizationOwner,
                            request.getOrganization(),
                            NotificationType.MEMBERSHIP_REJECTED,
                            "Organization Request Rejected",
                            request.getMemberOrganization()
                                    .getOrganizationName()
                                    + "'s request to join "
                                    + request.getOrganization()
                                    .getOrganizationName()
                                    + " has been rejected.",
                            request.getId()
                    );
                }
            }


            return successResponse(
                    "Membership request rejected successfully."
            );
        }


        /*
         * INVITATION:
         *
         * User invitation:
         * invited user rejects.
         *
         * Organization invitation:
         * owner of invited organization rejects.
         */
        if (request.getRequestType()
                == RequestType.INVITATION) {

            if (request.getUser() != null) {

                if (!request.getUser().getId()
                        .equals(currentUser.getId())) {

                    throw new RuntimeException(
                            "Only the invited user can "
                                    + "reject this invitation."
                    );
                }


                request.setStatus(
                        MembershipRequestStatus.REJECTED
                );

                request.setProcessedBy(currentUser);

                request.setProcessedAt(
                        LocalDateTime.now()
                );


                membershipRequestRepository.save(request);


                notificationService.sendNotification(
                        currentUser,
                        request.getOrganization(),
                        NotificationType.MEMBERSHIP_REJECTED,
                        "Invitation Rejected",
                        currentUser.getName()
                                + " has rejected the invitation "
                                + "from "
                                + request.getOrganization()
                                .getOrganizationName()
                                + ".",
                        request.getId()
                );


                return successResponse(
                        "Invitation rejected successfully."
                );
            }


            if (request.getMemberOrganization() != null) {

                validateOrganizationOwner(
                        request.getMemberOrganization().getId(),
                        currentUser
                );


                request.setStatus(
                        MembershipRequestStatus.REJECTED
                );

                request.setProcessedBy(currentUser);

                request.setProcessedAt(
                        LocalDateTime.now()
                );


                membershipRequestRepository.save(request);


                UserEntity inviterOwner =
                        request.getOrganization()
                                .getCreatedBy();


                if (inviterOwner != null) {

                    notificationService.sendNotification(
                            inviterOwner,
                            request.getOrganization(),
                            NotificationType.MEMBERSHIP_REJECTED,
                            "Organization Invitation Rejected",
                            request.getMemberOrganization()
                                    .getOrganizationName()
                                    + " has rejected your invitation.",
                            request.getId()
                    );
                }


                return successResponse(
                        "Organization invitation rejected successfully."
                );
            }
        }


        throw new RuntimeException(
                "Invalid membership request."
        );
    }


    // ============================================================
    // GET ORGANIZATION REQUESTS
    // ============================================================

    @Override
    public ApiResponse<List<MembershipRequestResponse>>
    getOrganizationRequests(
            Long organizationId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        validateOrganizationOwner(
                organizationId,
                currentUser
        );


        List<MembershipRequestResponse> response =
                membershipRequestRepository
                        .findAllByOrganizationIdAndStatus(
                                organizationId,
                                MembershipRequestStatus.PENDING
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();


        return ApiResponse
                .<List<MembershipRequestResponse>>builder()
                .success(true)
                .message(
                        "Membership requests fetched successfully."
                )
                .data(response)
                .build();
    }


    // ============================================================
    // GET MY INVITATIONS
    // ============================================================

    @Override
    public ApiResponse<List<MembershipRequestResponse>>
    getMyInvitations() {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        List<MembershipRequestResponse> response =
                new ArrayList<>(
                        membershipRequestRepository
                                .findAllByUserIdAndRequestType(
                                        currentUser.getId(),
                                        RequestType.INVITATION
                                )
                                .stream()
                                .map(this::toResponse)
                                .toList()
                );


        /*
         * Organization invitations require the current user
         * to be the owner of the invited organization.
         *
         * We find organizations created by this user.
         */
//        List<OrganizationEntity> ownedOrganizations =
//                organizationRepository
//                        .findAll()
//                        .stream()
//                        .filter(
//                                organization ->
//                                        organization.getCreatedBy() != null
//                                                && organization
//                                                .getCreatedBy()
//                                                .getId()
//                                                .equals(
//                                                        currentUser.getId()
//                                                )
//                        )
//                        .toList();

        List<OrganizationEntity> ownedOrganizations =
                organizationRepository
                        .findAllByCreatedById(
                                currentUser.getId()
                        );
        for (OrganizationEntity organization
                : ownedOrganizations) {

            response.addAll(
                    membershipRequestRepository
                            .findAllByMemberOrganizationIdAndRequestType(
                                    organization.getId(),
                                    RequestType.INVITATION
                            )
                            .stream()
                            .map(this::toResponse)
                            .toList()
            );
        }


        return ApiResponse
                .<List<MembershipRequestResponse>>builder()
                .success(true)
                .message(
                        "Invitations fetched successfully."
                )
                .data(response)
                .build();
    }


    // ============================================================
    // GET MY JOIN REQUESTS
    // ============================================================

    @Override
    public ApiResponse<List<MembershipRequestResponse>>
    getMyJoinRequests() {

        UserEntity currentUser =
                securityUtils.getCurrentUser();


        List<MembershipRequestResponse> response =
                new ArrayList<>(
                        membershipRequestRepository
                                .findAllByUserIdAndRequestType(
                                        currentUser.getId(),
                                        RequestType.JOIN_REQUEST
                                )
                                .stream()
                                .map(this::toResponse)
                                .toList()
                );


        /*
         * Also include organization-level join requests
         * sent by organizations owned by this user.
         */
        List<OrganizationEntity> ownedOrganizations =
                organizationRepository
                        .findAll()
                        .stream()
                        .filter(
                                organization ->
                                        organization.getCreatedBy() != null
                                                && organization
                                                .getCreatedBy()
                                                .getId()
                                                .equals(
                                                        currentUser.getId()
                                                )
                        )
                        .toList();


        for (OrganizationEntity organization
                : ownedOrganizations) {

            response.addAll(
                    membershipRequestRepository
                            .findAllByMemberOrganizationIdAndRequestType(
                                    organization.getId(),
                                    RequestType.JOIN_REQUEST
                            )
                            .stream()
                            .map(this::toResponse)
                            .toList()
            );
        }


        return ApiResponse
                .<List<MembershipRequestResponse>>builder()
                .success(true)
                .message(
                        "Join requests fetched successfully."
                )
                .data(response)
                .build();
    }


    // ============================================================
    // HELPERS
    // ============================================================

    private MembershipRequestEntity getPendingRequest(
            Long requestId) {

        MembershipRequestEntity request =
                membershipRequestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Membership request not found."
                                )
                        );


        if (request.getStatus()
                != MembershipRequestStatus.PENDING) {

            throw new RuntimeException(
                    "Request has already been processed."
            );
        }


        return request;
    }


    private void validateOrganizationOwner(
            Long organizationId,
            UserEntity user) {

        OrganizationMemberEntity owner =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                user.getId(),
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not an active member "
                                                + "of this organization."
                                )
                        );


        if (owner.getRole()
                != OrganizationRole.OWNER) {

            throw new RuntimeException(
                    "Only organization owner can perform "
                            + "this operation."
            );
        }
    }


    private MembershipRequestResponse toResponse(
            MembershipRequestEntity request) {

        MembershipRequestResponse.MembershipRequestResponseBuilder builder =
                MembershipRequestResponse.builder()
                        .requestId(request.getId())
                        .organizationId(
                                request.getOrganization().getId()
                        )
                        .organizationName(
                                request.getOrganization()
                                        .getOrganizationName()
                        )
                        .status(request.getStatus())
                        .requestType(request.getRequestType())
                        .createdAt(request.getCreatedAt());


        if (request.getUser() != null) {

            builder
                    .userId(
                            request.getUser().getId()
                    )
                    .userName(
                            request.getUser().getName()
                    );
        }


        if (request.getMemberOrganization() != null) {

            builder
                    .memberOrganizationId(
                            request.getMemberOrganization()
                                    .getId()
                    )
                    .memberOrganizationName(
                            request.getMemberOrganization()
                                    .getOrganizationName()
                    );
        }


        return builder.build();
    }


    private ApiResponse<String> successResponse(
            String message) {

        return ApiResponse
                .<String>builder()
                .success(true)
                .message(message)
                .data(message)
                .build();
    }
}