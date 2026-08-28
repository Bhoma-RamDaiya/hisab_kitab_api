package com.hisabkitab.backend.worker.domain;

import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.order.domain.ProductionSubmissionEntity;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "worker_earnings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_worker_earning_submission",
                        columnNames = "submission_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_worker_earning_worker",
                        columnList = "worker_id"
                ),
                @Index(
                        name = "idx_worker_earning_organization",
                        columnList = "organization_id"
                ),
                @Index(
                        name = "idx_worker_earning_submission",
                        columnList = "submission_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerEarningEntity {

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
     * One reviewed production submission can generate
     * only one earning record.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "submission_id",
            nullable = false,
            unique = true
    )
    private ProductionSubmissionEntity submission;

    /*
     * Accepted quantity that generated this earning.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal acceptedQuantity;

    /*
     * Historical worker rate copied from the submission.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal workerRate;

    /*
     * acceptedQuantity × workerRate
     */
    @Column(
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal earningAmount;

    @CreationTimestamp
    private LocalDateTime createdAt;
}