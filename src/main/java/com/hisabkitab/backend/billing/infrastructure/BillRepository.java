package com.hisabkitab.backend.billing.infrastructure;

import com.hisabkitab.backend.billing.domain.BillEntity;
import com.hisabkitab.backend.billing.domain.BillStatus;
import com.hisabkitab.backend.customer.domain.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BillRepository
        extends JpaRepository<BillEntity, Long> {

    List<BillEntity> findAllByCustomerId(
            Long customerId
    );

    Optional<BillEntity> findByIdAndCustomerId(
            Long billId,
            Long customerId
    );

    boolean existsByBillNumber(String billNumber);
    boolean existsByItemsOrderId(Long orderId);

    Optional<BillEntity> findByIdAndOrganizationId(
            Long billId,
            Long organizationId
    );

    List<BillEntity> findAllByOrganizationIdAndCustomerId(
            Long organizationId,
            Long customerId
    );

    List<BillEntity>
    findAllByOrganizationIdAndCustomerIdAndBillDateBefore(
            Long organizationId,
            Long customerId,
            LocalDateTime date
    );

    List<BillEntity>
    findAllByOrganizationIdAndCustomerIdAndBillDateBetween(
            Long organizationId,
            Long customerId,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("""
    SELECT COALESCE(SUM(b.totalAmount), 0)
    FROM BillEntity b
    WHERE b.organization.id = :organizationId
      AND b.customer.id = :customerId
      AND b.billDate < :date
""")
    BigDecimal getTotalBilledBeforeDate(
            Long organizationId,
            Long customerId,
            LocalDateTime date
    );

    @Query("""
        SELECT COALESCE(SUM(b.totalAmount), 0)
        FROM BillEntity b
        WHERE b.organization.id = :organizationId
          AND b.customer.id = :customerId
          AND b.status <> com.hisabkitab.backend.billing.domain.BillStatus.DRAFT
          AND b.status <> com.hisabkitab.backend.billing.domain.BillStatus.CANCELLED
        """)
    BigDecimal sumActiveBillsByCustomer(
            @Param("organizationId") Long organizationId,
            @Param("customerId") Long customerId
    );

    Optional<BillEntity>
    findByIdAndOrganizationIdAndCustomerId(
            Long billId,
            Long organizationId,
            Long customerId
    );
    List<BillEntity>
    findAllByOrganizationIdAndCustomerIdAndStatusInOrderByBillDateDesc(
            Long organizationId,
            Long customerId,
            List<BillStatus> statuses
    );

    List<BillEntity>
    findAllByOrganizationIdAndCustomerIdAndStatusInOrderByBillDateAsc(
            Long organizationId,
            Long customerId,
            List<BillStatus> statuses
    );

    @Query("""
        SELECT COALESCE(SUM(b.totalAmount), 0)
        FROM BillEntity b
        WHERE b.organization.id = :organizationId
          AND b.customer.id = :customerId
          AND b.status <> com.hisabkitab.backend.billing.domain.BillStatus.CANCELLED
        """)
    BigDecimal sumBillsByCustomer(
            @Param("organizationId") Long organizationId,
            @Param("customerId") Long customerId
    );

    boolean existsByOrganizationIdAndItemsOrderId(
            Long organizationId,
            Long orderId
    );
}