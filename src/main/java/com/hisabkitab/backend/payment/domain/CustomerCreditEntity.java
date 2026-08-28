package com.hisabkitab.backend.payment.domain;

import com.hisabkitab.backend.billing.domain.BillPaymentEntity;
import com.hisabkitab.backend.customer.domain.CustomerEntity;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "customer_credits",
        indexes = {
                @Index(
                        name = "idx_customer_credit_organization",
                        columnList = "organization_id"
                ),
                @Index(
                        name = "idx_customer_credit_customer",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_customer_credit_payment",
                        columnList = "payment_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Organization that received the customer payment.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organization_id",
            nullable = false
    )
    private OrganizationEntity organization;

    /**
     * Customer who has the credit.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private CustomerEntity customer;

    /**
     * Original payment that created this credit.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private BillPaymentEntity payment;

    /**
     * Original amount added as customer credit.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    /**
     * Amount of this credit that is still available.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal remainingAmount;

    /**
     * When this credit was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 1000)
    private String remarks;

    @OneToMany(
            mappedBy = "credit",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<CustomerCreditAllocationEntity> allocations =
            new ArrayList<>();
}