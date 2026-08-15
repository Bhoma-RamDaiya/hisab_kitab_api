package com.hisabkitab.backend.dto.response;

import com.hisabkitab.backend.appenum.RegistrationStatus;
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