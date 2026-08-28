package com.hisabkitab.backend.membership.application;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.membership.interfaces.MembershipRequestRequest;
import com.hisabkitab.backend.membership.interfaces.MembershipRequestResponse;

import java.util.List;


public interface MembershipRequestService {

    ApiResponse<String> sendMembershipRequest(
            MembershipRequestRequest request);


    ApiResponse<String> approveMembershipRequest(Long requestId);

    ApiResponse<String> rejectMembershipRequest(Long requestId);
    ApiResponse<List<MembershipRequestResponse>> getOrganizationRequests(
            Long organizationId);

    ApiResponse<List<MembershipRequestResponse>> getMyInvitations();

    ApiResponse<List<MembershipRequestResponse>> getMyJoinRequests();
}