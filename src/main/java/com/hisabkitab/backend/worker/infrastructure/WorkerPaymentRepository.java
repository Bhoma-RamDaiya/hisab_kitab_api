package com.hisabkitab.backend.worker.infrastructure;

import com.hisabkitab.backend.worker.domain.WorkerPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface WorkerPaymentRepository
        extends JpaRepository<WorkerPaymentEntity, Long> {

    List<WorkerPaymentEntity>
    findAllByWorkerIdOrderByPaidAtDesc(
            Long workerId
    );

    List<WorkerPaymentEntity>
    findAllByOrganizationIdOrderByPaidAtDesc(
            Long organizationId
    );

    List<WorkerPaymentEntity>
    findAllByOrganizationIdAndWorkerIdOrderByPaidAtDesc(
            Long organizationId,
            Long workerId
    );

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM WorkerPaymentEntity p
        WHERE p.organization.id = :organizationId
          AND p.worker.id = :workerId
        """)
    BigDecimal sumPaymentsByWorker(
            @Param("organizationId") Long organizationId,
            @Param("workerId") Long workerId
    );
}