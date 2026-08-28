package com.hisabkitab.backend.membership.domain;

import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import com.hisabkitab.backend.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "organization_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_organization_user",
                        columnNames = {
                                "organization_id",
                                "user_id"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_organization_member_organization",
                        columnNames = {
                                "organization_id",
                                "member_organization_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Organization to which the member belongs.
     *
     * Example:
     * Organization A
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organization_id",
            nullable = false
    )
    private OrganizationEntity organization;

    /**
     * Individual user member.
     *
     * Null when this membership represents
     * another organization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /**
     * Organization member.
     *
     * Example:
     *
     * Organization A
     *      ↓
     * Organization B
     *
     * From Organization A's perspective,
     * Organization B is its member/worker.
     *
     * Null when this membership represents
     * an individual user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_organization_id")
    private OrganizationEntity memberOrganization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

    /**
     * User who created/approved this membership.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(length = 500)
    private String exitRemark;
}