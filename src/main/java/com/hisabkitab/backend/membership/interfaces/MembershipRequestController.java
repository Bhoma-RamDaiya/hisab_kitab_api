package com.hisabkitab.backend.membership.interfaces;


import com.hisabkitab.backend.membership.application.MembershipRequestService;
import com.hisabkitab.backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/membership-requests")
@RequiredArgsConstructor
public class MembershipRequestController {

    private final MembershipRequestService membershipRequestService;

    @PostMapping("/send_request")
    public ApiResponse<String> sendMembershipRequest(
             @RequestBody MembershipRequestRequest request) {

        return membershipRequestService.sendMembershipRequest(request);
    }

    @PutMapping("/{requestId}/approve")
    public ApiResponse<String> approveMembershipRequest(
            @PathVariable Long requestId) {

        return membershipRequestService.approveMembershipRequest(requestId);
    }

    @PutMapping("/{requestId}/reject")
    public ApiResponse<String> rejectMembershipRequest(
            @PathVariable Long requestId) {

        return membershipRequestService.rejectMembershipRequest(requestId);
    }

    @GetMapping("/organizations/{organizationId}")
    public ApiResponse<List<MembershipRequestResponse>> getOrganizationRequests(
            @PathVariable Long organizationId) {

        return membershipRequestService.getOrganizationRequests(organizationId);
    }

    @GetMapping("/my")
    public ApiResponse<List<MembershipRequestResponse>> getMyInvitations() {

        return membershipRequestService.getMyInvitations();
    }

    @GetMapping("/my-requests")
    public ApiResponse<List<MembershipRequestResponse>> getMyJoinRequests() {

        return membershipRequestService.getMyJoinRequests();
    }
}