package com.hisabkitab.backend.payment.domain;

import com.hisabkitab.backend.billing.domain.BillEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "customer_credit_allocations",
        indexes = {
                @Index(
                        name = "idx_credit_allocation_credit",
                        columnList = "credit_id"
                ),
                @Index(
                        name = "idx_credit_allocation_bill",
                        columnList = "bill_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreditAllocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Customer credit being consumed.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "credit_id",
            nullable = false
    )
    private CustomerCreditEntity credit;

    /**
     * Bill against which the customer credit was used.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "bill_id",
            nullable = false
    )
    private BillEntity bill;

    /**
     * Amount of customer credit used for this bill.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    /**
     * When the credit was applied.
     */
    @Column(nullable = false)
    private LocalDateTime allocatedAt;

    /**
     * Optional explanation/audit note.
     */
    @Column(length = 1000)
    private String remarks;
}