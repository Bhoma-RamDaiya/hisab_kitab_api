package com.hisabkitab.backend.worker.application;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerProductRateRequest;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerProductRateResponse;

public interface WorkerProductRateService {

    ApiResponse<WorkerProductRateResponse> createRate(
            Long organizationId,
            Long workerId,
            Long productId,
            WorkerProductRateRequest request
    );

    ApiResponse<WorkerProductRateResponse> getRate(
            Long organizationId,
            Long workerId,
            Long productId
    );

    ApiResponse<WorkerProductRateResponse> updateRate(
            Long organizationId,
            Long workerId,
            Long productId,
            WorkerProductRateRequest request
    );
}