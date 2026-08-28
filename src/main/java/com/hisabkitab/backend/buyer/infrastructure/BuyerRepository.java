package com.hisabkitab.backend.buyer.infrastructure;

import com.hisabkitab.backend.buyer.interfaces.BuyerStatus;
import com.hisabkitab.backend.buyer.domain.BuyerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuyerRepository
        extends JpaRepository<BuyerEntity, Long> {

    List<BuyerEntity> findAllByCustomerId(Long customerId);

    List<BuyerEntity> findAllByCustomerIdAndStatus(
            Long customerId,
            BuyerStatus status
    );

    Optional<BuyerEntity> findByIdAndCustomerId(
            Long buyerId,
            Long customerId
    );

    boolean existsByCustomerIdAndNameIgnoreCase(
            Long customerId,
            String name
    );

    boolean existsByCustomerIdAndNameIgnoreCaseAndIdNot(
            Long customerId,
            String name,
            Long buyerId
    );
}