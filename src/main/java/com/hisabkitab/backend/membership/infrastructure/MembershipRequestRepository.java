package com.hisabkitab.backend.membership.infrastructure;

import com.hisabkitab.backend.membership.domain.MembershipRequestEntity;
import com.hisabkitab.backend.membership.domain.MembershipRequestStatus;
import com.hisabkitab.backend.membership.interfaces.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipRequestRepository
        extends JpaRepository<MembershipRequestEntity, Long> {

    /*
     * Requests received by an organization.
     *
     * Example:
     *
     * Organization A
     *      ↑
     *      │
     * User X / Organization B
     */
    List<MembershipRequestEntity>
    findAllByOrganizationIdAndStatus(
            Long organizationId,
            MembershipRequestStatus status
    );


    /*
     * User's invitations / join requests.
     */
    List<MembershipRequestEntity>
    findAllByUserIdAndRequestType(
            Long userId,
            RequestType requestType
    );


    /*
     * Organization's invitations / join requests.
     *
     * memberOrganizationId represents the organization
     * that is being invited or that sent the request.
     */
    List<MembershipRequestEntity>
    findAllByMemberOrganizationIdAndRequestType(
            Long memberOrganizationId,
            RequestType requestType
    );


    /*
     * Prevent duplicate pending requests between
     * an organization and a user.
     */
    boolean existsByOrganizationIdAndUserIdAndStatus(
            Long organizationId,
            Long userId,
            MembershipRequestStatus status
    );


    /*
     * Prevent duplicate pending requests between
     * two organizations.
     */
    boolean existsByOrganizationIdAndMemberOrganizationIdAndStatus(
            Long organizationId,
            Long memberOrganizationId,
            MembershipRequestStatus status
    );
}