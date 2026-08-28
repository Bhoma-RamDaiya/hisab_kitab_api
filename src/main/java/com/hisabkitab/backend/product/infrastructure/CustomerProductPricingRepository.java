package com.hisabkitab.backend.product.infrastructure;

import com.hisabkitab.backend.product.domain.CustomerProductPricingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerProductPricingRepository
        extends JpaRepository<CustomerProductPricingEntity, Long> {

    Optional<CustomerProductPricingEntity>
    findByOrganizationIdAndCustomerIdAndProductId(
            Long organizationId,
            Long customerId,
            Long productId
    );

    Optional<CustomerProductPricingEntity>
    findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<CustomerProductPricingEntity>
    findAllByOrganizationIdAndCustomerId(
            Long organizationId,
            Long customerId
    );
}