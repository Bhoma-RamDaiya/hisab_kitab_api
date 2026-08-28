package com.hisabkitab.backend.billing.infrastructure;

import com.hisabkitab.backend.billing.domain.BillPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BillPaymentRepository
        extends JpaRepository<BillPaymentEntity, Long> {

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM BillPaymentEntity p
            WHERE p.organization.id = :organizationId
              AND p.customer.id = :customerId
            """)
    BigDecimal sumPaymentsByCustomer(
            @Param("organizationId") Long organizationId,
            @Param("customerId") Long customerId
    );

    List<BillPaymentEntity>
    findAllByOrganizationIdAndCustomerIdOrderByPaidAtDesc(
            Long organizationId,
            Long customerId
    );

    List<BillPaymentEntity>
    findAllByOrganizationIdOrderByPaidAtDesc(
            Long organizationId
    );

    Optional<BillPaymentEntity>
    findByIdAndOrganizationId(
            Long paymentId,
            Long organizationId
    );

    List<BillPaymentEntity>
    findAllByOrganizationIdAndCustomerIdAndPaidAtBefore(
            Long organizationId,
            Long customerId,
            LocalDateTime date
    );

    List<BillPaymentEntity>
    findAllByOrganizationIdAndCustomerIdAndPaidAtBetween(
            Long organizationId,
            Long customerId,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM BillPaymentEntity p
            WHERE p.organization.id = :organizationId
              AND p.customer.id = :customerId
              AND p.paidAt < :date
            """)
    BigDecimal getTotalPaidBeforeDate(
            @Param("organizationId") Long organizationId,
            @Param("customerId") Long customerId,
            @Param("date") LocalDateTime date
    );
}