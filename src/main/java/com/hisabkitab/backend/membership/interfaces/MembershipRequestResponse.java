package com.hisabkitab.backend.membership.interfaces;

import com.hisabkitab.backend.membership.domain.MembershipRequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipRequestResponse {

    private Long requestId;

    /*
     * Organization receiving the request.
     */
    private Long organizationId;
    private String organizationName;

    /*
     * Individual user involved in the request.
     *
     * NULL for organization-to-organization requests.
     */
    private Long userId;
    private String userName;

    /*
     * Organization involved as the member/worker.
     *
     * NULL for user-to-organization requests.
     */
    private Long memberOrganizationId;
    private String memberOrganizationName;

    private MembershipRequestStatus status;

    private RequestType requestType;

    private LocalDateTime createdAt;
}