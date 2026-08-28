package com.hisabkitab.backend.organization.interfaces.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationRegistrationResponse {

    private Long registrationId;

    private Long applicantId;

    private String applicantName;

    private String applicantUsername;

    private String organizationName;

    private String ownerName;

    private String mobileNumber;

    private RegistrationStatus status;

    private LocalDateTime createdAt;
}