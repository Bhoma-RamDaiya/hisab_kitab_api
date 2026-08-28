package com.hisabkitab.backend.product.infrastructure;

import com.hisabkitab.backend.product.domain.MeasurementDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeasurementDefinitionRepository
        extends JpaRepository<MeasurementDefinitionEntity, Long> {

    List<MeasurementDefinitionEntity>
    findAllByOrganizationIdAndActiveTrue(
            Long organizationId
    );

    Optional<MeasurementDefinitionEntity>
    findByIdAndOrganizationId(
            Long measurementId,
            Long organizationId
    );

    boolean existsByOrganizationIdAndNameIgnoreCase(
            Long organizationId,
            String name
    );
}