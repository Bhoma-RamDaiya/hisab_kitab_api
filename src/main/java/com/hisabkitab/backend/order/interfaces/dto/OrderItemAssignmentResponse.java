package com.hisabkitab.backend.order.interfaces.dto;

import com.hisabkitab.backend.order.domain.OrderItemAssignmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemAssignmentResponse {

    private Long id;

    private Long orderItemId;

    private Long workerId;

    private String workerName;

    private BigDecimal assignedQuantity;

    private BigDecimal completedQuantity;

    private BigDecimal rejectedQuantity;

    private BigDecimal remainingQuantity;

    private BigDecimal pendingReworkQuantity;

    private OrderItemAssignmentStatus status;

    private LocalDateTime assignedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String notes;
}