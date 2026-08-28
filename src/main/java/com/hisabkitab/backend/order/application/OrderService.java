package com.hisabkitab.backend.order.application;

import com.hisabkitab.backend.order.domain.OrderStatus;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.order.interfaces.dto.OrderRequest;
import com.hisabkitab.backend.order.interfaces.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    ApiResponse<OrderResponse> createOrder(
            Long organizationId,
            Long customerId,
            OrderRequest request
    );

    ApiResponse<List<OrderResponse>> getOrders(
            Long organizationId,
            Long customerId
    );

    ApiResponse<OrderResponse> getOrder(
            Long organizationId,
            Long customerId,
            Long orderId
    );

    ApiResponse<OrderResponse> updateOrder(
            Long organizationId,
            Long customerId,
            Long orderId,
            OrderRequest request
    );

    ApiResponse<String> updateOrderStatus(
            Long organizationId,
            Long customerId,
            Long orderId,
            OrderStatus status
    );
}