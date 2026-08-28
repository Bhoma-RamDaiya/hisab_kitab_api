package com.hisabkitab.backend.billing.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "bill_payment_allocations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_bill_allocation",
                        columnNames = {
                                "payment_id",
                                "bill_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_payment_allocation_payment",
                        columnList = "payment_id"
                ),
                @Index(
                        name = "idx_payment_allocation_bill",
                        columnList = "bill_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillPaymentAllocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "payment_id",
            nullable = false
    )
    private BillPaymentEntity payment;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "bill_id",
            nullable = false
    )
    private BillEntity bill;

    @Column(
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal amount;
}