package com.hisabkitab.backend.order.infrastructure;

import com.hisabkitab.backend.order.domain.ProductionSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductionSubmissionRepository
        extends JpaRepository<ProductionSubmissionEntity, Long> {

    List<ProductionSubmissionEntity> findAllByAssignmentIdOrderBySubmittedAtAsc(
            Long assignmentId
    );
    List<ProductionSubmissionEntity>
    findAllByWorkerIdOrderBySubmittedAtAsc(
            Long workerId
    );

    Optional<ProductionSubmissionEntity>
    findByIdAndAssignmentOrderItemOrderOrganizationId(
            Long submissionId,
            Long organizationId
    );
    Optional<ProductionSubmissionEntity>
    findByIdAndAssignmentIdAndAssignmentOrderItemOrderOrganizationId(
            Long submissionId,
            Long assignmentId,
            Long organizationId
    );
}