package com.hisabkitab.backend.order.interfaces.rest;

import com.hisabkitab.backend.order.domain.OrderStatus;
import com.hisabkitab.backend.order.interfaces.dto.OrderRequest;
import com.hisabkitab.backend.order.interfaces.dto.OrderResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.order.application.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/organizations/{organizationId}/customers/{customerId}/orders"
)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @Valid @RequestBody OrderRequest request) {

        return ResponseEntity.ok(
                orderService.createOrder(
                        organizationId,
                        customerId,
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
            @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                orderService.getOrders(
                        organizationId,
                        customerId
                )
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                orderService.getOrder(
                        organizationId,
                        customerId,
                        orderId
                )
        );
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @Valid @RequestBody OrderRequest request) {

        return ResponseEntity.ok(
                orderService.updateOrder(
                        organizationId,
                        customerId,
                        orderId,
                        request
                )
        );
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<String>> updateOrderStatus(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(
                        organizationId,
                        customerId,
                        orderId,
                        status
                )
        );
    }
}