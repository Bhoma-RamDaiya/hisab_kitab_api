package com.hisabkitab.backend.worker.application;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.utils.SecurityUtils;
import com.hisabkitab.backend.worker.domain.WorkerPaymentEntity;
import com.hisabkitab.backend.worker.infrastructure.WorkerEarningRepository;
import com.hisabkitab.backend.worker.infrastructure.WorkerPaymentRepository;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerPaymentRequest;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkerPaymentServiceImpl
        implements WorkerPaymentService {

    private final WorkerPaymentRepository workerPaymentRepository;

    private final WorkerEarningRepository workerEarningRepository;

    private final OrganizationMemberRepository
            organizationMemberRepository;

    private final SecurityUtils securityUtils;

    @Override
    public ApiResponse<WorkerPaymentResponse> createPayment(
            Long organizationId,
            Long workerId,
            WorkerPaymentRequest request) {

        /*
         * The logged-in user must belong to this organization.
         */
        OrganizationMemberEntity payer =
                getCurrentOrganizationMember(
                        organizationId
                );

        validateWorkerPaymentAccess(payer);

        /*
         * The worker must be an active member of
         * this organization.
         *
         * PESSIMISTIC_WRITE lock is acquired here so
         * concurrent payment requests for the same
         * worker cannot both pass the payable check.
         */
        OrganizationMemberEntity worker =
                organizationMemberRepository
                        .findWorkerForPayment(
                                workerId,
                                organizationId,
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Worker is not an active member of this organization."
                                ));

        BigDecimal paymentAmount =
                request.getAmount();

        /*
         * Calculate total amount earned by this worker.
         */
        BigDecimal totalEarned =
                workerEarningRepository.sumEarningsByWorker(
                        organizationId,
                        workerId
                );

        if (totalEarned == null) {
            totalEarned = BigDecimal.ZERO;
        }

        /*
         * Calculate all payments already made.
         */
        BigDecimal totalPaid =
                workerPaymentRepository.sumPaymentsByWorker(
                        organizationId,
                        workerId
                );

        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }

        /*
         * Calculate current payable balance.
         */
        BigDecimal pendingPayable =
                totalEarned.subtract(totalPaid);

        /*
         * The accounting ledger must never have
         * more paid than earned.
         */
        if (pendingPayable.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Worker payment ledger is inconsistent."
            );
        }

        /*
         * Never allow payment greater than the
         * worker's pending payable.
         */
        if (paymentAmount.compareTo(
                pendingPayable) > 0) {

            throw new RuntimeException(
                    "Payment amount exceeds worker's pending payable. "
                            + "Pending payable: "
                            + pendingPayable
            );
        }

        /*
         * Payment date.
         *
         * If the client does not provide one,
         * use the current date/time.
         */
        LocalDateTime paidAt =
                request.getPaidAt() != null
                        ? request.getPaidAt()
                        : LocalDateTime.now();

        /*
         * Create payment ledger entry.
         */
        WorkerPaymentEntity payment =
                WorkerPaymentEntity.builder()
                        .organization(
                                worker.getOrganization()
                        )
                        .worker(worker)
                        .amount(paymentAmount)
                        .paidAt(paidAt)
                        .paymentReference(
                                request.getPaymentReference()
                        )
                        .remarks(
                                request.getRemarks()
                        )
                        .build();

        payment =
                workerPaymentRepository.save(
                        payment
                );

        return ApiResponse
                .<WorkerPaymentResponse>builder()
                .success(true)
                .message(
                        "Worker payment recorded successfully."
                )
                .data(
                        toResponse(payment)
                )
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
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<WorkerPaymentResponse>> getWorkerPayments(
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

        List<WorkerPaymentResponse> payments =
                workerPaymentRepository
                        .findAllByOrganizationIdAndWorkerIdOrderByPaidAtDesc(
                                organizationId,
                                workerId
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse
                .<List<WorkerPaymentResponse>>builder()
                .success(true)
                .message("Worker payment history fetched successfully.")
                .data(payments)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<WorkerPaymentResponse>>
    getOrganizationPayments(
            Long organizationId) {

        getCurrentOrganizationMember(organizationId);

        List<WorkerPaymentResponse> payments =
                workerPaymentRepository
                        .findAllByOrganizationIdOrderByPaidAtDesc(
                                organizationId
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse
                .<List<WorkerPaymentResponse>>builder()
                .success(true)
                .message(
                        "Organization payment history fetched successfully."
                )
                .data(payments)
                .build();
    }
    private WorkerPaymentResponse toResponse(
            WorkerPaymentEntity payment) {

        String workerName;

        if (payment.getWorker().getUser() != null) {

            workerName =
                    payment.getWorker()
                            .getUser()
                            .getName();

        } else if (
                payment.getWorker()
                        .getMemberOrganization() != null) {

            workerName =
                    payment.getWorker()
                            .getMemberOrganization()
                            .getOrganizationName();

        } else {

            workerName = "Unknown Worker";
        }

        return WorkerPaymentResponse.builder()
                .id(payment.getId())
                .workerId(
                        payment.getWorker().getId()
                )
                .workerName(workerName)
                .amount(
                        payment.getAmount()
                )
                .paymentReference(
                        payment.getPaymentReference()
                )
                .remarks(
                        payment.getRemarks()
                )
                .paidAt(
                        payment.getPaidAt()
                )
                .createdAt(
                        payment.getCreatedAt()
                )
                .build();
    }

    private void validateWorkerPaymentAccess(
            OrganizationMemberEntity member) {

        OrganizationRole role =
                member.getRole();

        if (role != OrganizationRole.OWNER
                && role != OrganizationRole.ADMIN
                && role != OrganizationRole.ACCOUNTANT) {

            throw new RuntimeException(
                    "You are not authorized to make worker payments."
            );
        }
    }
}