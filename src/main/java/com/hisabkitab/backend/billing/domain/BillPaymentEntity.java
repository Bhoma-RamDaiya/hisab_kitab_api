package com.hisabkitab.backend.billing.domain;

import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "bill_payments",
        indexes = {
                @Index(
                        name = "idx_bill_payment_organization",
                        columnList = "organization_id"
                ),
                @Index(
                        name = "idx_bill_payment_customer",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_bill_payment_paid_at",
                        columnList = "paid_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organization_id",
            nullable = false
    )
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private CustomerEntity customer;

    @Column(
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    @Column(length = 100)
    private String paymentReference;

    @OneToMany(
            mappedBy = "payment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<BillPaymentAllocationEntity> allocations =
            new ArrayList<>();

    @Column(length = 1000)
    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdAt;
}