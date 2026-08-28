package com.hisabkitab.backend.worker.application;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.utils.SecurityUtils;
import com.hisabkitab.backend.worker.domain.WorkerEarningEntity;
import com.hisabkitab.backend.worker.infrastructure.WorkerEarningRepository;
import com.hisabkitab.backend.worker.infrastructure.WorkerPaymentRepository;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerEarningResponse;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerPayableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkerEarningServiceImpl
        implements WorkerEarningService {

    private final WorkerEarningRepository workerEarningRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final SecurityUtils securityUtils;
    private final WorkerPaymentRepository workerPaymentRepository;
    @Override
    public ApiResponse<List<WorkerEarningResponse>>
    getWorkerEarnings(
            Long organizationId,
            Long workerId) {

        getCurrentOrganizationMember(organizationId);

        organizationMemberRepository
                .findByIdAndOrganizationIdAndStatus(
                        workerId,
                        organizationId,
                        MembershipStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Worker is not an active member of this organization."
                        ));

        List<WorkerEarningResponse> responses =
                workerEarningRepository
                        .findAllByWorkerIdOrderByCreatedAtDesc(
                                workerId
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse
                .<List<WorkerEarningResponse>>builder()
                .success(true)
                .message(
                        "Worker earnings fetched successfully."
                )
                .data(responses)
                .build();
    }

    @Override
    public ApiResponse<List<WorkerEarningResponse>>
    getOrganizationEarnings(
            Long organizationId) {

        getCurrentOrganizationMember(organizationId);

        List<WorkerEarningResponse> responses =
                workerEarningRepository
                        .findAllByOrganizationIdOrderByCreatedAtDesc(
                                organizationId
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse
                .<List<WorkerEarningResponse>>builder()
                .success(true)
                .message(
                        "Organization earnings fetched successfully."
                )
                .data(responses)
                .build();
    }

    @Override
    public ApiResponse<WorkerEarningResponse>
    getEarning(
            Long organizationId,
            Long earningId) {

        getCurrentOrganizationMember(organizationId);

        WorkerEarningEntity earning =
                workerEarningRepository
                        .findByIdAndOrganizationId(
                                earningId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Worker earning not found."
                                ));

        return ApiResponse
                .<WorkerEarningResponse>builder()
                .success(true)
                .message(
                        "Worker earning fetched successfully."
                )
                .data(toResponse(earning))
                .build();
    }

    @Override
    public ApiResponse<WorkerPayableResponse> getWorkerPayable(
            Long organizationId,
            Long workerId) {

        getCurrentOrganizationMember(organizationId);

        OrganizationMemberEntity worker =
                organizationMemberRepository
                        .findByIdAndOrganizationIdAndStatus(
                                workerId,
                                organizationId,
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Worker is not an active member of this organization."
                                ));

        BigDecimal totalEarned =
                workerEarningRepository.sumEarningsByWorker(
                        organizationId,
                        workerId
                );

        BigDecimal totalPaid =
                workerPaymentRepository.sumPaymentsByWorker(
                        organizationId,
                        workerId
                );

        BigDecimal pendingPayable =
                totalEarned
                        .subtract(totalPaid)
                        .max(BigDecimal.ZERO);

        String workerName;

        if (worker.getUser() != null) {

            workerName =
                    worker.getUser().getName();

        } else if (
                worker.getMemberOrganization() != null) {

            workerName =
                    worker.getMemberOrganization()
                            .getOrganizationName();

        } else {

            workerName = "Unknown Worker";
        }

        WorkerPayableResponse response =
                WorkerPayableResponse.builder()
                        .workerId(worker.getId())
                        .workerName(workerName)
                        .totalEarned(totalEarned)
                        .totalPaid(totalPaid)
                        .pendingPayable(pendingPayable)
                        .build();

        return ApiResponse
                .<WorkerPayableResponse>builder()
                .success(true)
                .message(
                        "Worker payable fetched successfully."
                )
                .data(response)
                .build();
    }
    private OrganizationMemberEntity
    getCurrentOrganizationMember(
            Long organizationId) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();

        return organizationMemberRepository
                .findByOrganizationIdAndUserIdAndStatus(
                        organizationId,
                        currentUser.getId(),
                        MembershipStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not an active member of this organization."
                        ));
    }

    private WorkerEarningResponse toResponse(
            WorkerEarningEntity earning) {

        String workerName;

        if (earning.getWorker().getUser() != null) {

            workerName =
                    earning.getWorker()
                            .getUser()
                            .getName();

        } else if (
                earning.getWorker()
                        .getMemberOrganization() != null) {

            workerName =
                    earning.getWorker()
                            .getMemberOrganization()
                            .getOrganizationName();

        } else {

            workerName = "Unknown Worker";
        }

        return WorkerEarningResponse.builder()
                .id(earning.getId())
                .workerId(
                        earning.getWorker().getId()
                )
                .workerName(workerName)
                .submissionId(
                        earning.getSubmission().getId()
                )
                .assignmentId(
                        earning.getSubmission()
                                .getAssignment()
                                .getId()
                )
                .acceptedQuantity(
                        earning.getAcceptedQuantity()
                )
                .workerRate(
                        earning.getWorkerRate()
                )
                .earningAmount(
                        earning.getEarningAmount()
                )
                .createdAt(
                        earning.getCreatedAt()
                )
                .build();
    }
}
