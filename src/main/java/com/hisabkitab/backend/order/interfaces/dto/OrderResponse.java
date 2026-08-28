package com.hisabkitab.backend.order.interfaces.dto;

import com.hisabkitab.backend.order.domain.OrderStatus;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;

    private Long organizationId;

    private Long customerId;

    private Long buyerId;

    private String buyerName;

    private LocalDateTime orderDate;

    private LocalDateTime completedAt;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private String notes;

    private List<OrderItemResponse> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}