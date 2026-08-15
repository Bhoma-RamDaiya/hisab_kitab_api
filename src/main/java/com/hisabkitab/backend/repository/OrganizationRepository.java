package com.hisabkitab.backend.repository;

import com.hisabkitab.backend.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity ,Long> {
    boolean existsByOrganizationCode(String organizationCode);
    boolean existsByPanNumber(String panNumber);
}
