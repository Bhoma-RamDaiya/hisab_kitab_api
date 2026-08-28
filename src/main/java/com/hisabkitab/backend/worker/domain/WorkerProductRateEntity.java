package com.hisabkitab.backend.worker.domain;

import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.product.domain.ProductEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "worker_product_rates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_worker_product_rate",
                        columnNames = {
                                "organization_member_id",
                                "product_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_worker_product_rate_worker",
                        columnList = "organization_member_id"
                ),
                @Index(
                        name = "idx_worker_product_rate_product",
                        columnList = "product_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProductRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Worker belonging to the organization.
     *
     * This can represent:
     * - an individual employee/user
     * - a member organization
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organization_member_id",
            nullable = false
    )
    private OrganizationMemberEntity worker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private ProductEntity product;

    /**
     * Special worker rate decided by the organization.
     *
     * If no worker-specific rate exists,
     * ProductPricingEntity.workerRate is used.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal rate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}