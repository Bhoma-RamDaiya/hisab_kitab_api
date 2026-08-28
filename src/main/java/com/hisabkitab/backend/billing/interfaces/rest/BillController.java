package com.hisabkitab.backend.billing.interfaces.rest;

import com.hisabkitab.backend.billing.interfaces.dto.BillRequest;
import com.hisabkitab.backend.billing.interfaces.dto.BillResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.billing.application.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/organizations/{organizationId}/customers/{customerId}/bills"
)
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<ApiResponse<BillResponse>> createBill(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @Valid @RequestBody BillRequest request) {

        return ResponseEntity.ok(
                billService.createBill(
                        organizationId,
                        customerId,
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BillResponse>>> getBills(
            @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                billService.getBills(
                        organizationId,
                        customerId
                )
        );
    }

    @GetMapping("/{billId}")
    public ResponseEntity<ApiResponse<BillResponse>> getBill(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @PathVariable Long billId) {

        return ResponseEntity.ok(
                billService.getBill(
                        organizationId,
                        customerId,
                        billId
                )
        );
    }

    @PatchMapping("/{billId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelBill(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @PathVariable Long billId) {

        return ResponseEntity.ok(
                billService.cancelBill(
                        organizationId,
                        customerId,
                        billId
                )
        );
    }
}