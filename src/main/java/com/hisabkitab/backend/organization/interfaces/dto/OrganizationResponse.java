package com.hisabkitab.backend.organization.interfaces.dto;

import com.hisabkitab.backend.user.interfaces.Status;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {

    private Long id;
    private String organizationName;
    private String organizationCode;
    private String address;
    private String phoneNumber;
    private String gstNumber;
    private String panNumber;
    private Status status;
}
