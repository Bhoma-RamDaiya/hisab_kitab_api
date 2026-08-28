package com.hisabkitab.backend.organization.domain;
import com.hisabkitab.backend.organization.interfaces.dto.RegistrationStatus;
import com.hisabkitab.backend.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name="organization_registrations")
public class OrganizationRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private UserEntity applicant;

    @Column(nullable = false)
    private String organizationName;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String panNumber;

    private String gstNumber;
    @Column(nullable = false)
    private String aadhaarNumber;

    @Column(nullable = false)
    private String mobileNumber;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;

    private String adminRemark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private UserEntity approvedBy;
    private LocalDateTime approvedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
