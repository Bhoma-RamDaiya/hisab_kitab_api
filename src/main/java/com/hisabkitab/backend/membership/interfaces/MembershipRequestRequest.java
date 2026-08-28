package com.hisabkitab.backend.membership.interfaces;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipRequestRequest {

    /*
     * Organization receiving the request.
     *
     * Example:
     * ABC Organization
     */
    private Long organizationId;


    /*
     * Used for User → Organization requests.
     *
     * NULL when the request involves
     * another organization.
     */
    private Long userId;


    /*
     * Used for Organization → Organization requests.
     *
     * NULL when the request involves
     * an individual user.
     */
    private Long memberOrganizationId;


    /*
     * Defines the direction/type of request.
     */
    private RequestType requestType;
}