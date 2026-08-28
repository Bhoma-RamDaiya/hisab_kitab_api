package com.hisabkitab.backend.membership.application;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.membership.interfaces.OrganizationMemberResponse;

import java.util.List;

public interface OrganizationMemberService {

    ApiResponse<List<OrganizationMemberResponse>> getMembers(
            Long organizationId);

    ApiResponse<String> blockMember(
            Long organizationId,
            Long userId);

    ApiResponse<String> unblockMember(
            Long organizationId,
            Long userId);

    ApiResponse<String> removeMember(
            Long organizationId,
            Long userId,
            String remark);

    ApiResponse<String> leaveOrganization(
            Long organizationId,
            String remark);
}
