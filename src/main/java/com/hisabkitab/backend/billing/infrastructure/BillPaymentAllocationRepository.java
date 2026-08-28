package com.hisabkitab.backend.billing.infrastructure;

import com.hisabkitab.backend.billing.domain.BillPaymentAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BillPaymentAllocationRepository
        extends JpaRepository<BillPaymentAllocationEntity, Long> {
    List<BillPaymentAllocationEntity> findAllByBillId(
            Long billId
    );

    List<BillPaymentAllocationEntity> findAllByPaymentId(
            Long paymentId
    );
    @Query("""
            SELECT COALESCE(SUM(a.amount), 0)
            FROM BillPaymentAllocationEntity a
            WHERE a.bill.id = :billId
            """)
    BigDecimal sumAllocatedAmountByBill(
            @Param("billId") Long billId
    );

    @Query("""
            SELECT COALESCE(SUM(a.amount), 0)
            FROM BillPaymentAllocationEntity a
            WHERE a.payment.id = :paymentId
            """)
    BigDecimal sumAllocatedAmountByPayment(
            @Param("paymentId") Long paymentId
    );
}