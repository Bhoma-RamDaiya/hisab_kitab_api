package com.hisabkitab.backend.repository;

import com.hisabkitab.backend.appenum.MembershipStatus;
import com.hisabkitab.backend.appenum.OrganizationRole;
import com.hisabkitab.backend.entity.OrganizationMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMemberEntity ,Long > {
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
}
