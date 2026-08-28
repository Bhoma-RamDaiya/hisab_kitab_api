package com.hisabkitab.backend.order.interfaces.rest;

import com.hisabkitab.backend.order.application.ProductionSubmissionService;
import com.hisabkitab.backend.order.interfaces.dto.ProductionReviewRequest;
import com.hisabkitab.backend.order.interfaces.dto.ProductionSubmissionRequest;
import com.hisabkitab.backend.order.interfaces.dto.ProductionSubmissionResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/organizations/{organizationId}/assignments"
)
@RequiredArgsConstructor
public class ProductionSubmissionController {

    private final ProductionSubmissionService productionSubmissionService;

    @PostMapping("/{assignmentId}/submissions")
    public ResponseEntity<ApiResponse<ProductionSubmissionResponse>>
    submitProduction(
            @PathVariable Long organizationId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody ProductionSubmissionRequest request) {

        return ResponseEntity.ok(
                productionSubmissionService.submitProduction(
                        organizationId,
                        assignmentId,
                        request
                )
        );
    }

    @GetMapping("/{assignmentId}/submissions")
    public ResponseEntity<ApiResponse<List<ProductionSubmissionResponse>>>
    getProductionHistory(
            @PathVariable Long organizationId,
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(
                productionSubmissionService.getProductionHistory(
                        organizationId,
                        assignmentId
                )
        );
    }



    @PostMapping(
            "/{assignmentId}/submissions/{submissionId}/review"
    )
    public ResponseEntity<ApiResponse<ProductionSubmissionResponse>>
    reviewProduction(
            @PathVariable Long organizationId,
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @Valid @RequestBody ProductionReviewRequest request) {

        return ResponseEntity.ok(
                productionSubmissionService.reviewProduction(
                        organizationId,
                        assignmentId,
                        submissionId,
                        request
                )
        );
    }

    @PostMapping(
            "/{assignmentId}/submissions/{submissionId}/rework"
    )
    public ResponseEntity<ApiResponse<ProductionSubmissionResponse>>
    submitRework(
            @PathVariable Long organizationId,
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @Valid @RequestBody ProductionSubmissionRequest request) {

        return ResponseEntity.ok(
                productionSubmissionService.submitRework(
                        organizationId,
                        assignmentId,
                        submissionId,
                        request
                )
        );
    }
}