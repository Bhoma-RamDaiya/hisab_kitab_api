package com.hisabkitab.backend.order.domain;

import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "order_item_assignments",
        indexes = {
                @Index(
                        name = "idx_assignment_order_item",
                        columnList = "order_item_id"
                ),
                @Index(
                        name = "idx_assignment_worker",
                        columnList = "worker_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_item_id",
            nullable = false
    )
    private OrderItemEntity orderItem;

    /**
     * Worker assigned to this order item.
     *
     * This membership can represent:
     *
     * 1. An individual employee/user
     * 2. Another organization that is a member
     *    of the parent organization.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "worker_id",
            nullable = false
    )
    private OrganizationMemberEntity worker;

    /**
     * Quantity assigned to this worker.
     */
    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal assignedQuantity;

    private LocalDateTime assignedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderItemAssignmentStatus status =
            OrderItemAssignmentStatus.ASSIGNED;

    /**
     * Production submissions made by this worker.
     */
    @OneToMany(
            mappedBy = "assignment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ProductionSubmissionEntity> submissions =
            new ArrayList<>();
}