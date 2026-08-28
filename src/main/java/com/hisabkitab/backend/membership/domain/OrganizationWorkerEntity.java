package com.hisabkitab.backend.membership.domain;

import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "organization_workers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_organization_worker",
                        columnNames = {
                                "organization_id",
                                "member_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_worker_organization",
                        columnList = "organization_id"
                ),
                @Index(
                        name = "idx_worker_member",
                        columnList = "member_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationWorkerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent organization that gives the work.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organization_id",
            nullable = false
    )
    private OrganizationEntity organization;

    /**
     * Organization member who acts as a worker.
     *
     * This member can represent:
     * - an individual employee
     * - a member organization
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private OrganizationMemberEntity member;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}