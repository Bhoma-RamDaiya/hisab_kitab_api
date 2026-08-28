package com.hisabkitab.backend.order.infrastructure;

import com.hisabkitab.backend.order.domain.OrderStatus;
import com.hisabkitab.backend.order.domain.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findAllByCustomerId(
            Long customerId
    );

    List<OrderEntity> findAllByCustomerIdAndStatus(
            Long customerId,
            OrderStatus status
    );

    Optional<OrderEntity> findByIdAndCustomerId(
            Long orderId,
            Long customerId
    );

    Optional<OrderEntity>
    findByIdAndOrganizationIdAndCustomerId(
            Long orderId,
            Long organizationId,
            Long customerId
    );
}