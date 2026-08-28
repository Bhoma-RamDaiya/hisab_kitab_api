package com.hisabkitab.backend.worker.interfaces.rest;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.worker.application.WorkerPaymentService;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerPaymentRequest;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerPaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations/{organizationId}")
@RequiredArgsConstructor
public class WorkerPaymentController {

    private final WorkerPaymentService workerPaymentService;

    @PostMapping("/workers/{workerId}/payments")
    public ResponseEntity<ApiResponse<WorkerPaymentResponse>>
    createPayment(
            @PathVariable Long organizationId,
            @PathVariable Long workerId,
            @Valid @RequestBody WorkerPaymentRequest request) {

        return ResponseEntity.ok(
                workerPaymentService.createPayment(
                        organizationId,
                        workerId,
                        request
                )
        );
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<WorkerPaymentResponse>>>
    getOrganizationPayments(
            @PathVariable Long organizationId) {

        return ResponseEntity.ok(
                workerPaymentService.getOrganizationPayments(
                        organizationId
                )
        );
    }
    @GetMapping("/workers/{workerId}/payments")
    public ResponseEntity<ApiResponse<List<WorkerPaymentResponse>>>
    getWorkerPayments(
            @PathVariable Long organizationId,
            @PathVariable Long workerId) {

        return ResponseEntity.ok(
                workerPaymentService.getWorkerPayments(
                        organizationId,
                        workerId
                )
        );
    }
}