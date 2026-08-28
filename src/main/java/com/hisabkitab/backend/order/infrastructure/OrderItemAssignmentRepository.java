package com.hisabkitab.backend.order.infrastructure;

import com.hisabkitab.backend.order.domain.OrderItemAssignmentEntity;
import com.hisabkitab.backend.order.domain.OrderItemAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderItemAssignmentRepository
        extends JpaRepository<OrderItemAssignmentEntity, Long> {

    List<OrderItemAssignmentEntity> findAllByOrderItemId(
            Long orderItemId
    );

    List<OrderItemAssignmentEntity> findAllByWorkerId(
            Long workerId
    );

    Optional<OrderItemAssignmentEntity>
    findByIdAndOrderItemOrderOrganizationId(
            Long assignmentId,
            Long organizationId
    );

    List<OrderItemAssignmentEntity>
    findAllByOrderItemOrderOrganizationId(
            Long organizationId
    );

    List<OrderItemAssignmentEntity>
    findAllByWorkerIdAndStatus(
            Long workerId,
            OrderItemAssignmentStatus status
    );

    List<OrderItemAssignmentEntity>
    findAllByWorkerIdAndOrderItemOrderOrganizationId(
            Long workerId,
            Long organizationId
    );
}