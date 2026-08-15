package com.hisabkitab.backend.entity;
import com.hisabkitab.backend.appenum.MembershipStatus;
import com.hisabkitab.backend.appenum.RequestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="organization_requests")
public class OrganizationRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    private MembershipStatus status;

    @Column(length = 500)
    private String message;


    @CreationTimestamp
    private LocalDateTime createdAt;


    private LocalDateTime respondedAt;

    @Column(length = 500)
    private String responseRemark;
}




