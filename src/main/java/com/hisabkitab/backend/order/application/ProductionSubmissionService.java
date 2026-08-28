package com.hisabkitab.backend.order.application;

import com.hisabkitab.backend.order.interfaces.dto.ProductionReviewRequest;
import com.hisabkitab.backend.order.interfaces.dto.ProductionSubmissionRequest;
import com.hisabkitab.backend.order.interfaces.dto.ProductionSubmissionResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;

import java.util.List;

public interface ProductionSubmissionService {

    ApiResponse<ProductionSubmissionResponse> submitProduction(
            Long organizationId,
            Long assignmentId,
            ProductionSubmissionRequest request
    );

    ApiResponse<List<ProductionSubmissionResponse>> getProductionHistory(
            Long organizationId,
            Long assignmentId
    );

    ApiResponse<ProductionSubmissionResponse> reviewProduction(
            Long organizationId,
            Long assignmentId,
            Long submissionId,
            ProductionReviewRequest request
    );
    ApiResponse<ProductionSubmissionResponse> submitRework(
            Long organizationId,
            Long assignmentId,
            Long submissionId,
            ProductionSubmissionRequest request
    );
}