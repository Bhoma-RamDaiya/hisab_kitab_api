package com.hisabkitab.backend.payment.infrastructure;

import com.hisabkitab.backend.payment.domain.CustomerCreditAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerCreditAllocationRepository
        extends JpaRepository<
        CustomerCreditAllocationEntity,
        Long> {

    @Query("""
            SELECT COALESCE(SUM(a.amount), 0)
            FROM CustomerCreditAllocationEntity a
            WHERE a.credit.id = :creditId
            """)
    BigDecimal sumAllocatedAmountByCredit(
            @Param("creditId") Long creditId
    );

    @Query("""
            SELECT COALESCE(SUM(a.amount), 0)
            FROM CustomerCreditAllocationEntity a
            WHERE a.bill.id = :billId
            """)
    BigDecimal sumCreditAllocatedToBill(
            @Param("billId") Long billId
    );

    List<CustomerCreditAllocationEntity>
    findAllByCreditOrganizationIdAndCreditCustomerIdOrderByAllocatedAtDesc(
            Long organizationId,
            Long customerId
    );
}