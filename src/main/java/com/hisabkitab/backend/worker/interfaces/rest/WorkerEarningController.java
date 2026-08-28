package com.hisabkitab.backend.worker.interfaces.rest;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.worker.application.WorkerEarningService;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerEarningResponse;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerPayableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations/{organizationId}")
@RequiredArgsConstructor
public class WorkerEarningController {

    private final WorkerEarningService workerEarningService;

    @GetMapping("/earnings")
    public ResponseEntity<ApiResponse<List<WorkerEarningResponse>>>
    getOrganizationEarnings(
            @PathVariable Long organizationId) {

        return ResponseEntity.ok(
                workerEarningService.getOrganizationEarnings(
                        organizationId
                )
        );
    }

    @GetMapping("/workers/{workerId}/earnings")
    public ResponseEntity<ApiResponse<List<WorkerEarningResponse>>>
    getWorkerEarnings(
            @PathVariable Long organizationId,
            @PathVariable Long workerId) {

        return ResponseEntity.ok(
                workerEarningService.getWorkerEarnings(
                        organizationId,
                        workerId
                )
        );
    }

    @GetMapping("/earnings/{earningId}")
    public ResponseEntity<ApiResponse<WorkerEarningResponse>>
    getEarning(
            @PathVariable Long organizationId,
            @PathVariable Long earningId) {

        return ResponseEntity.ok(
                workerEarningService.getEarning(
                        organizationId,
                        earningId
                )
        );
    }

    @GetMapping("/workers/{workerId}/payable")
    public ResponseEntity<ApiResponse<WorkerPayableResponse>>
    getWorkerPayable(
            @PathVariable Long organizationId,
            @PathVariable Long workerId) {

        return ResponseEntity.ok(
                workerEarningService.getWorkerPayable(
                        organizationId,
                        workerId
                )
        );
    }
}