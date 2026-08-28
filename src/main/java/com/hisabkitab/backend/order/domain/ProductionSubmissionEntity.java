package com.hisabkitab.backend.order.domain;

import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "production_submissions",
        indexes = {
                @Index(
                        name = "idx_submission_assignment",
                        columnList = "assignment_id"
                ),
                @Index(
                        name = "idx_submission_worker",
                        columnList = "worker_id"
                ),
                @Index(
                        name = "idx_submission_submitted_by",
                        columnList = "submitted_by"
                ),
                @Index(
                        name = "idx_submission_reviewed_by",
                        columnList = "reviewed_by"
                ),
                @Index(
                        name = "idx_submission_submitted_at",
                        columnList = "submitted_at"
                ),
                @Index(
                        name = "idx_submission_rework_of",
                        columnList = "rework_of_submission_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionSubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Assignment for which this production was submitted.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "assignment_id",
            nullable = false
    )
    private OrderItemAssignmentEntity assignment;

    /**
     * Worker responsible for the assignment.
     *
     * This can represent:
     * - an individual employee
     * - a member organization
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "worker_id",
            nullable = false
    )
    private OrganizationMemberEntity worker;

    /**
     * Actual user who submitted this production.
     *
     * This is important for the audit trail because the worker
     * may be a member organization.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "submitted_by",
            nullable = false
    )
    private UserEntity submittedBy;

    /**
     * Quantity physically submitted by the worker.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal submittedQuantity;

    /**
     * Quantity accepted by the organization.
     *
     * Starts at zero because acceptance happens during review.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    @Builder.Default
    private BigDecimal acceptedQuantity = BigDecimal.ZERO;

    /**
     * Quantity rejected by the organization.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    @Builder.Default
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    /**
     * Worker rate applicable to this production
     * when it was submitted.
     *
     * This is a historical snapshot.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal workerRate;

    /**
     * When the worker submitted the production.
     */
    @Column(nullable = false)
    private LocalDateTime submittedAt;

    /**
     * When the organization reviewed the submission.
     */
    private LocalDateTime reviewedAt;

    /**
     * Actual user who reviewed this submission.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserEntity reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductionSubmissionStatus status =
            ProductionSubmissionStatus.SUBMITTED;

    /**
     * Notes provided by the worker during submission.
     */
    @Column(length = 1000)
    private String submissionNotes;

    /**
     * Notes provided by the reviewer.
     */
    @Column(length = 1000)
    private String reviewNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rework_of_submission_id")
    private ProductionSubmissionEntity reworkOf;
}