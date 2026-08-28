package com.hisabkitab.backend.order.interfaces.rest;

import com.hisabkitab.backend.order.application.OrderItemAssignmentService;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemAssignmentResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations/{organizationId}/employees")
@RequiredArgsConstructor
public class WorkerAssignmentController {

    private final OrderItemAssignmentService assignmentService;

    @GetMapping("/{employeeId}/assignments")
    public ResponseEntity<ApiResponse<List<OrderItemAssignmentResponse>>>
    getWorkerAssignments(
            @PathVariable Long organizationId,
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                assignmentService.getWorkerAssignments(
                        organizationId,
                        employeeId
                )
        );
    }
}
