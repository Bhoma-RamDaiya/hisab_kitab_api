package com.hisabkitab.backend.worker.domain;

import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "worker_payments",
        indexes = {
                @Index(
                        name = "idx_worker_payment_worker",
                        columnList = "worker_id"
                ),
                @Index(
                        name = "idx_worker_payment_organization",
                        columnList = "organization_id"
                ),
                @Index(
                        name = "idx_worker_payment_paid_at",
                        columnList = "paid_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerPaymentEntity {

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
            name = "worker_id",
            nullable = false
    )
    private OrganizationMemberEntity worker;

    /*
     * Actual amount paid by organization.
     */
    @Column(
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal amount;

    /*
     * When payment was made.
     */
    @Column(nullable = false)
    private LocalDateTime paidAt;

    /*
     * Optional payment reference.
     */
    @Column(length = 100)
    private String paymentReference;

    /*
     * Optional remarks.
     */
    @Column(length = 1000)
    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdAt;
}