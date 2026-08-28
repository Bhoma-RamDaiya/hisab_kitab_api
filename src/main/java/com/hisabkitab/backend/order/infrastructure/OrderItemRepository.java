package com.hisabkitab.backend.order.infrastructure;

import com.hisabkitab.backend.order.domain.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository
        extends JpaRepository<OrderItemEntity, Long> {

    Optional<OrderItemEntity> findByIdAndOrderOrganizationId(
            Long orderItemId,
            Long organizationId
    );
}