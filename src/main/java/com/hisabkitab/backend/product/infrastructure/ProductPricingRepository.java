package com.hisabkitab.backend.product.infrastructure;

import com.hisabkitab.backend.product.domain.ProductPricingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductPricingRepository
        extends JpaRepository<ProductPricingEntity, Long> {

    Optional<ProductPricingEntity>
    findByOrganizationIdAndProductId(
            Long organizationId,
            Long productId
    );

    Optional<ProductPricingEntity>
    findByIdAndOrganizationId(
            Long pricingId,
            Long organizationId
    );
    Optional<ProductPricingEntity> findByOrganizationIdAndProductIdAndActiveTrue(
            Long organizationId,
            Long productId
    );
}