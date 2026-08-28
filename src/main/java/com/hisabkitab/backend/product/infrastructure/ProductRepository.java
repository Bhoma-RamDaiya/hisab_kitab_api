package com.hisabkitab.backend.product.infrastructure;


import com.hisabkitab.backend.product.domain.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findAllByOrganizationIdAndActiveTrue(
            Long organizationId
    );

    Optional<ProductEntity> findByIdAndOrganizationId(
            Long productId,
            Long organizationId
    );

    boolean existsByOrganizationIdAndNameIgnoreCase(
            Long organizationId,
            String name
    );
}
