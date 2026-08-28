package com.hisabkitab.backend.customer.interfaces.dto;

import com.hisabkitab.backend.customer.domain.CustomerCategory;
import com.hisabkitab.backend.customer.domain.CustomerStatus;
import com.hisabkitab.backend.customer.domain.CustomerType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;

    private Long organizationId;

    private CustomerType customerType;

    private String customerName;

    private Long linkedUserId;

    private Long linkedOrganizationId;

    private String phoneNumber;

    private String email;

    private String address;

    private CustomerStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private CustomerCategory category;
}