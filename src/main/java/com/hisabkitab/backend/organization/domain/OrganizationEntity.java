package com.hisabkitab.backend.organization.domain;
import com.hisabkitab.backend.billing.domain.PaymentAllocationMethod;
import com.hisabkitab.backend.user.interfaces.Status;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String organizationName;

    @Column(unique = true, nullable = false)
    private String organizationCode;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    private String gstNumber;

    @Column(nullable = false)
    private String panNumber;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @OneToMany(mappedBy = "organization")
    private List<OrganizationMemberEntity> members;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_allocation_method",
            nullable = false
    )
    @Builder.Default
    private PaymentAllocationMethod paymentAllocationMethod =
            PaymentAllocationMethod.LATEST_BILL_FIRST;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}