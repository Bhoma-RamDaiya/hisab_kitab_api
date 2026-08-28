package com.hisabkitab.backend.membership.infrastructure;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationMemberRepository
        extends JpaRepository<OrganizationMemberEntity, Long> {

    boolean existsByUserIdAndRoleAndStatus(
            Long userId,
            OrganizationRole role,
            MembershipStatus status
    );

    boolean existsByUserIdAndRole(
            Long userId,
            OrganizationRole role
    );

    int countByUserIdAndRoleAndStatus(
            Long userId,
            OrganizationRole role,
            MembershipStatus status
    );

    Optional<OrganizationMemberEntity>
    findByOrganizationIdAndUserIdAndStatus(
            Long organizationId,
            Long userId,
            MembershipStatus status
    );

    List<OrganizationMemberEntity>
    findAllByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            MembershipStatus status
    );

    boolean existsByOrganizationIdAndUserId(
            Long organizationId,
            Long userId
    );

    /*
     * Checks whether another organization is already
     * a member/worker of this organization.
     */
    boolean existsByOrganizationIdAndMemberOrganizationId(
            Long organizationId,
            Long memberOrganizationId
    );

    List<OrganizationMemberEntity>
    findAllByOrganizationId(
            Long organizationId
    );

    Optional<OrganizationMemberEntity>
    findByOrganizationIdAndUserId(
            Long organizationId,
            Long userId
    );

    Optional<OrganizationMemberEntity>
    findByIdAndOrganizationIdAndStatus(
            Long memberId,
            Long organizationId,
            MembershipStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT m
        FROM OrganizationMemberEntity m
        WHERE m.id = :workerId
          AND m.organization.id = :organizationId
          AND m.status = :status
        """)
    Optional<OrganizationMemberEntity> findWorkerForPayment(
            @Param("workerId") Long workerId,
            @Param("organizationId") Long organizationId,
            @Param("status") MembershipStatus status
    );

    boolean existsByOrganizationIdAndMemberOrganizationIdAndStatus(
            Long organizationId,
            Long memberOrganizationId,
            MembershipStatus status
    );
}