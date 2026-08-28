package com.hisabkitab.backend.worker.infrastructure;

import com.hisabkitab.backend.worker.domain.WorkerProductRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkerProductRateRepository
        extends JpaRepository<WorkerProductRateEntity, Long> {

    Optional<WorkerProductRateEntity>
    findByWorkerIdAndProductId(
            Long workerId,
            Long productId
    );

    Optional<WorkerProductRateEntity>
    findByIdAndWorkerId(
            Long rateId,
            Long workerId
    );

    List<WorkerProductRateEntity>
    findAllByWorkerId(
            Long workerId
    );

    List<WorkerProductRateEntity>
    findAllByWorkerIdAndActiveTrue(
            Long workerId
    );
}