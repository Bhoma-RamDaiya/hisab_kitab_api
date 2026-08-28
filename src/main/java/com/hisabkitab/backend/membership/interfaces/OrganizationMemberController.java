package com.hisabkitab.backend.membership.interfaces;

import com.hisabkitab.backend.membership.application.OrganizationMemberService;
import com.hisabkitab.backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations/{organizationId}/members")
@RequiredArgsConstructor
public class OrganizationMemberController {

    private final OrganizationMemberService organizationMemberService;

    @GetMapping
    public ApiResponse<List<OrganizationMemberResponse>> getMembers(
            @PathVariable Long organizationId) {

        return organizationMemberService.getMembers(organizationId);
    }

    @PutMapping("/{userId}/block")
    public ApiResponse<String> blockMember(
            @PathVariable Long organizationId,
            @PathVariable Long userId) {

        return organizationMemberService.blockMember(
                organizationId,
                userId
        );
    }

    @PutMapping("/{userId}/unblock")
    public ApiResponse<String> unblockMember(
            @PathVariable Long organizationId,
            @PathVariable Long userId) {

        return organizationMemberService.unblockMember(
                organizationId,
                userId
        );
    }

    @PutMapping("/{userId}/remove")
    public ApiResponse<String> removeMember(
            @PathVariable Long organizationId,
            @PathVariable Long userId,
            @RequestParam(required = false) String remark) {

        return organizationMemberService.removeMember(
                organizationId,
                userId,
                remark
        );
    }

    @PutMapping("/leave")
    public ApiResponse<String> leaveOrganization(
            @PathVariable Long organizationId,
            @RequestParam(required = false) String remark) {

        return organizationMemberService.leaveOrganization(
                organizationId,
                remark
        );
    }
}
