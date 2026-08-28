package com.hisabkitab.backend.worker.infrastructure;

import com.hisabkitab.backend.worker.domain.WorkerEarningEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WorkerEarningRepository
        extends JpaRepository<WorkerEarningEntity, Long> {

    Optional<WorkerEarningEntity>
    findBySubmissionId(Long submissionId);

    boolean existsBySubmissionId(Long submissionId);

    List<WorkerEarningEntity>
    findAllByWorkerIdOrderByCreatedAtDesc(
            Long workerId
    );

    List<WorkerEarningEntity>
    findAllByOrganizationIdOrderByCreatedAtDesc(
            Long organizationId
    );

    Optional<WorkerEarningEntity>
    findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    @Query("""
        SELECT COALESCE(SUM(e.earningAmount), 0)
        FROM WorkerEarningEntity e
        WHERE e.organization.id = :organizationId
          AND e.worker.id = :workerId
        """)
    BigDecimal sumEarningsByWorker(
            @Param("organizationId") Long organizationId,
            @Param("workerId") Long workerId
    );
}