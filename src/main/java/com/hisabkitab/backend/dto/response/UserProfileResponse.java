package com.hisabkitab.backend.dto.response;

import com.hisabkitab.backend.appenum.Gender;
import com.hisabkitab.backend.appenum.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private Long id;

    private String name;

    private String username;

    private String email;

    private String phoneNumber;

    private Gender gender;

    private UserRole userRole;
    private boolean hasOrganization;
    private int organizationCount;
}
