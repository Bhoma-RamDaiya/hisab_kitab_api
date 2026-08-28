package com.hisabkitab.backend.worker.application;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerEarningResponse;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerPayableResponse;

import java.util.List;

public interface WorkerEarningService {

    ApiResponse<List<WorkerEarningResponse>>
    getWorkerEarnings(
            Long organizationId,
            Long workerId
    );

    ApiResponse<List<WorkerEarningResponse>>
    getOrganizationEarnings(
            Long organizationId
    );

    ApiResponse<WorkerEarningResponse>
    getEarning(
            Long organizationId,
            Long earningId
    );

    ApiResponse<WorkerPayableResponse> getWorkerPayable(
            Long organizationId,
            Long workerId
    );
}