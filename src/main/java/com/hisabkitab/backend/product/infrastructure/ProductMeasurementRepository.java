package com.hisabkitab.backend.product.infrastructure;

import com.hisabkitab.backend.product.domain.ProductMeasurementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductMeasurementRepository
        extends JpaRepository<ProductMeasurementEntity, Long> {

    List<ProductMeasurementEntity> findAllByProductIdOrderByDisplayOrderAsc(
            Long productId
    );
}