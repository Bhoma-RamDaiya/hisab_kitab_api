package com.hisabkitab.backend.organization.infrastructure;

import com.hisabkitab.backend.organization.domain.OrganizationRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRegistrationRepository extends JpaRepository<OrganizationRegistrationEntity,Long > {
}
