package com.hisabkitab.backend.customer.domain;



import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "customers",
        indexes = {
                @Index(name = "idx_customer_organization", columnList = "organization_id"),
                @Index(name = "idx_customer_phone", columnList = "phone_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The organization whose customer this is.
     *
     * Example:
     * ABC Tailors -> Customer = XYZ Garments
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType customerType;

    @Column(nullable = false)
    private String customerName;

    /**
     * If the customer is a registered Hisab Kitab user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_user_id")
    private UserEntity linkedUser;

    /**
     * If the customer itself is another Hisab Kitab organization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_organization_id")
    private OrganizationEntity linkedOrganization;

    private String phoneNumber;

    private String email;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CustomerCategory category = CustomerCategory.ONE_TIME;
}
