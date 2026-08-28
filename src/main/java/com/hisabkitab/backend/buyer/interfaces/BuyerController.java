package com.hisabkitab.backend.buyer.interfaces;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.buyer.application.BuyerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/organizations/{organizationId}/customers/{customerId}/buyers"
)
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @PostMapping
    public ResponseEntity<ApiResponse<BuyerResponse>> createBuyer(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @Valid @RequestBody BuyerRequest request) {

        return ResponseEntity.ok(
                buyerService.createBuyer(
                        organizationId,
                        customerId,
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BuyerResponse>>> getBuyers(
            @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                buyerService.getBuyers(
                        organizationId,
                        customerId
                )
        );
    }

    @GetMapping("/{buyerId}")
    public ResponseEntity<ApiResponse<BuyerResponse>> getBuyer(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @PathVariable Long buyerId) {

        return ResponseEntity.ok(
                buyerService.getBuyer(
                        organizationId,
                        customerId,
                        buyerId
                )
        );
    }

    @PutMapping("/{buyerId}")
    public ResponseEntity<ApiResponse<BuyerResponse>> updateBuyer(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @PathVariable Long buyerId,
            @Valid @RequestBody BuyerRequest request) {

        return ResponseEntity.ok(
                buyerService.updateBuyer(
                        organizationId,
                        customerId,
                        buyerId,
                        request
                )
        );
    }

    @PatchMapping("/{buyerId}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateBuyer(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @PathVariable Long buyerId) {

        return ResponseEntity.ok(
                buyerService.deactivateBuyer(
                        organizationId,
                        customerId,
                        buyerId
                )
        );
    }
}