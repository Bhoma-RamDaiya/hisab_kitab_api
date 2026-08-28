package com.hisabkitab.backend.order.interfaces.rest;

import com.hisabkitab.backend.order.application.OrderItemAssignmentService;
import com.hisabkitab.backend.order.interfaces.dto.CompleteAssignmentRequest;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemAssignmentRequest;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemAssignmentResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/organizations/{organizationId}/order-items"
)
@RequiredArgsConstructor
public class OrderItemAssignmentController {

    private final OrderItemAssignmentService assignmentService;

    @PostMapping("/{orderItemId}/assignments")
    public ResponseEntity<ApiResponse<OrderItemAssignmentResponse>>
    assignWorker(
            @PathVariable Long organizationId,
            @PathVariable Long orderItemId,
            @Valid @RequestBody OrderItemAssignmentRequest request) {

        return ResponseEntity.ok(
                assignmentService.assignWorker(
                        organizationId,
                        orderItemId,
                        request
                )
        );
    }

    @GetMapping("/{orderItemId}/assignments")
    public ResponseEntity<ApiResponse<List<OrderItemAssignmentResponse>>>
    getAssignments(
            @PathVariable Long organizationId,
            @PathVariable Long orderItemId) {

        return ResponseEntity.ok(
                assignmentService.getAssignments(
                        organizationId,
                        orderItemId
                )
        );
    }

    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<OrderItemAssignmentResponse>>
    getAssignment(
            @PathVariable Long organizationId,
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                assignmentService.getAssignment(
                        organizationId,
                        assignmentId
                )
        );
    }

    @PutMapping("/assignments/{assignmentId}/start")
    public ResponseEntity<ApiResponse<String>>
    startAssignment(
            @PathVariable Long organizationId,
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                assignmentService.startAssignment(
                        organizationId,
                        assignmentId
                )
        );
    }


}