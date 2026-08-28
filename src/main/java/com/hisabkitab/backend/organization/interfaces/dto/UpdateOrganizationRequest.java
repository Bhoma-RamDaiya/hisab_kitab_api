package com.hisabkitab.backend.organization.interfaces.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrganizationRequest {

    private String organizationName;
    private String address;
    private String phoneNumber;
    private String gstNumber;
}