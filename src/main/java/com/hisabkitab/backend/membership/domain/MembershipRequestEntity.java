package com.hisabkitab.backend.membership.domain;

import com.hisabkitab.backend.membership.interfaces.RequestType;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "membership_requests"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * Organization receiving the request.
     *
     * Example:
     *
     * ABC Organization
     *       ↑
     *       │ request
     *       │
     * XYZ Organization / User
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organization_id",
            nullable = false
    )
    private OrganizationEntity organization;


    /**
     * Individual user involved in the request.
     *
     * NULL when the request is
     * Organization → Organization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;


    /**
     * Organization involved in the request.
     *
     * NULL when the request is
     * User → Organization.
     *
     * Example:
     *
     * ABC Organization
     *       ↑
     *       │
     * XYZ Organization
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_organization_id")
    private OrganizationEntity memberOrganization;


    /**
     * Current state of the membership request.
     *
     * PENDING
     * APPROVED
     * REJECTED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipRequestStatus status;


    /**
     * Type of membership request.
     *
     * JOIN_REQUEST
     * INVITATION
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType requestType;


    /**
     * User who approved or rejected the request.
     *
     * This is intentionally named processedBy because
     * the request can either be APPROVED or REJECTED.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private UserEntity processedBy;


    /**
     * Date and time when the request was approved or rejected.
     */
    private LocalDateTime processedAt;


    /**
     * Optional remark provided while processing
     * the membership request.
     */
    @Column(length = 500)
    private String remark;


    @CreationTimestamp
    private LocalDateTime createdAt;


    @UpdateTimestamp
    private LocalDateTime updatedAt;


    /**
     * A membership request must represent exactly
     * ONE target type.
     *
     * User request:
     *
     * user != null
     * memberOrganization == null
     *
     *
     * Organization request:
     *
     * user == null
     * memberOrganization != null
     */
    @PrePersist
    @PreUpdate
    private void validateRequestTarget() {

        boolean hasUser =
                user != null;

        boolean hasOrganization =
                memberOrganization != null;

        if (hasUser == hasOrganization) {

            throw new IllegalStateException(
                    "Membership request must contain either "
                            + "a user or an organization, but not both."
            );
        }
    }
}