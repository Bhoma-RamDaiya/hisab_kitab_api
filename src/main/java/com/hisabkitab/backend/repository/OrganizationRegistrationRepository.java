package com.hisabkitab.backend.repository;

import com.hisabkitab.backend.entity.OrganizationRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRegistrationRepository extends JpaRepository<OrganizationRegistrationEntity,Long > {
}
