package com.hisabkitab.backend.product.interfaces.rest;

import com.hisabkitab.backend.product.application.ProductService;
import com.hisabkitab.backend.product.interfaces.dto.CreateMeasurementRequest;
import com.hisabkitab.backend.product.interfaces.dto.MeasurementResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations/{organizationId}/measurements")
@RequiredArgsConstructor
public class MeasurementController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<MeasurementResponse>> createMeasurement(
            @PathVariable Long organizationId,
            @Valid @RequestBody CreateMeasurementRequest request) {

        return ResponseEntity.ok(
                productService.createMeasurement(
                        organizationId,
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MeasurementResponse>>> getMeasurements(
            @PathVariable Long organizationId) {

        return ResponseEntity.ok(
                productService.getMeasurements(
                        organizationId
                )
        );
    }
}