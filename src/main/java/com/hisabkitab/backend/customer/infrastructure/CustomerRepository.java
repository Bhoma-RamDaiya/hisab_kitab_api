package com.hisabkitab.backend.customer.infrastructure;

import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.customer.domain.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<CustomerEntity, Long> {

    List<CustomerEntity> findAllByOrganizationId(
            Long organizationId
    );

    List<CustomerEntity> findAllByOrganizationIdAndStatus(
            Long organizationId,
            CustomerStatus status
    );

    Optional<CustomerEntity> findByIdAndOrganizationId(
            Long customerId,
            Long organizationId
    );

    boolean existsByOrganizationIdAndCustomerNameIgnoreCase(
            Long organizationId,
            String customerName
    );
    boolean existsByOrganizationIdAndCustomerNameIgnoreCaseAndIdNot(
            Long organizationId,
            String customerName,
            Long customerId
    );
}