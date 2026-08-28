package com.hisabkitab.backend.user.interfaces;

import com.hisabkitab.backend.user.domain.Gender;
import com.hisabkitab.backend.user.domain.UserRole;
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
