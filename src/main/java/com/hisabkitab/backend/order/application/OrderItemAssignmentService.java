package com.hisabkitab.backend.order.application;

import com.hisabkitab.backend.order.interfaces.dto.CompleteAssignmentRequest;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemAssignmentRequest;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemAssignmentResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;

import java.util.List;

public interface OrderItemAssignmentService {

    ApiResponse<OrderItemAssignmentResponse> assignWorker(
            Long organizationId,
            Long orderItemId,
            OrderItemAssignmentRequest request
    );

    ApiResponse<List<OrderItemAssignmentResponse>> getAssignments(
            Long organizationId,
            Long orderItemId
    );

    ApiResponse<OrderItemAssignmentResponse> getAssignment(
            Long organizationId,
            Long assignmentId
    );

    ApiResponse<String> startAssignment(
            Long organizationId,
            Long assignmentId
    );

    ApiResponse<List<OrderItemAssignmentResponse>> getWorkerAssignments(
            Long organizationId,
            Long employeeId
    );
}