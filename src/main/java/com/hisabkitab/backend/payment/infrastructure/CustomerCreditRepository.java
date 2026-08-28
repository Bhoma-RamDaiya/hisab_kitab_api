package com.hisabkitab.backend.payment.infrastructure;

import com.hisabkitab.backend.payment.domain.CustomerCreditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerCreditRepository
        extends JpaRepository<CustomerCreditEntity, Long> {

    @Query("""
            SELECT COALESCE(SUM(c.remainingAmount), 0)
            FROM CustomerCreditEntity c
            WHERE c.organization.id = :organizationId
              AND c.customer.id = :customerId
              AND c.remainingAmount > 0
            """)
    BigDecimal sumAvailableCredit(
            @Param("organizationId") Long organizationId,
            @Param("customerId") Long customerId
    );

    List<CustomerCreditEntity>
    findAllByOrganizationIdAndCustomerIdAndRemainingAmountGreaterThanOrderByCreatedAtAsc(
            Long organizationId,
            Long customerId,
            BigDecimal amount
    );

    @Query("""
        SELECT COALESCE(SUM(c.amount), 0)
        FROM CustomerCreditEntity c
        WHERE c.organization.id = :organizationId
          AND c.customer.id = :customerId
        """)
    BigDecimal sumTotalCredit(
            @Param("organizationId") Long organizationId,
            @Param("customerId") Long customerId
    );
}