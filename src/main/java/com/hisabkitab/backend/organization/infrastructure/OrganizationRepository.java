package com.hisabkitab.backend.organization.infrastructure;

import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository
        extends JpaRepository<OrganizationEntity, Long> {

    boolean existsByOrganizationCode(
            String organizationCode
    );

    boolean existsByPanNumber(
            String panNumber
    );

    /*
     * Returns organizations created/owned by the given user.
     *
     * Used for finding organizations represented by
     * the currently logged-in user.
     */
    List<OrganizationEntity> findAllByCreatedById(
            Long userId
    );
}