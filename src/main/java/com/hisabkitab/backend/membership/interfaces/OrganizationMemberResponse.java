package com.hisabkitab.backend.membership.interfaces;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationMemberResponse {

    private Long userId;

    private String name;

    private String username;

    private OrganizationRole role;

    private MembershipStatus status;

    private LocalDateTime joinedAt;
}
