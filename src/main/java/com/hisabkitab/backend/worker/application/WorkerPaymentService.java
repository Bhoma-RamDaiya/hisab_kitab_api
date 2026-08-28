package com.hisabkitab.backend.worker.application;


import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerPaymentRequest;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerPaymentResponse;

import java.util.List;

public interface WorkerPaymentService {

    ApiResponse<WorkerPaymentResponse> createPayment(
            Long organizationId,
            Long workerId,
            WorkerPaymentRequest request
    );

    ApiResponse<List<WorkerPaymentResponse>> getWorkerPayments(
            Long organizationId,
            Long workerId
    );

    ApiResponse<List<WorkerPaymentResponse>> getOrganizationPayments(
            Long organizationId
    );



}