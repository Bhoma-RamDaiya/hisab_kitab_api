package com.hisabkitab.backend.order.application;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.order.domain.OrderItemAssignmentEntity;
import com.hisabkitab.backend.order.domain.OrderItemAssignmentStatus;
import com.hisabkitab.backend.order.domain.ProductionSubmissionEntity;
import com.hisabkitab.backend.order.domain.ProductionSubmissionStatus;
import com.hisabkitab.backend.order.infrastructure.OrderItemAssignmentRepository;
import com.hisabkitab.backend.order.infrastructure.ProductionSubmissionRepository;
import com.hisabkitab.backend.order.interfaces.dto.ProductionReviewRequest;
import com.hisabkitab.backend.order.interfaces.dto.ProductionSubmissionRequest;
import com.hisabkitab.backend.order.interfaces.dto.ProductionSubmissionResponse;
import com.hisabkitab.backend.organization.domain.OrganizationRole;
import com.hisabkitab.backend.product.domain.ProductPricingEntity;
import com.hisabkitab.backend.product.infrastructure.ProductPricingRepository;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.utils.SecurityUtils;
import com.hisabkitab.backend.worker.domain.WorkerEarningEntity;
import com.hisabkitab.backend.worker.domain.WorkerProductRateEntity;
import com.hisabkitab.backend.worker.infrastructure.WorkerEarningRepository;
import com.hisabkitab.backend.worker.infrastructure.WorkerProductRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductionSubmissionServiceImpl
        implements ProductionSubmissionService {

    private final ProductionSubmissionRepository submissionRepository;
    private final OrderItemAssignmentRepository assignmentRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final SecurityUtils securityUtils;
    private final WorkerProductRateRepository workerProductRateRepository;
    private final ProductPricingRepository productPricingRepository;
    private final WorkerEarningRepository workerEarningRepository;
    @Override
    public ApiResponse<ProductionSubmissionResponse> submitProduction(
            Long organizationId,
            Long assignmentId,
            ProductionSubmissionRequest request) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();

        /*
         * Get current user's active membership.
         */
        OrganizationMemberEntity currentMember =
                getCurrentOrganizationMember(
                        organizationId,
                        currentUser.getId()
                );

        /*
         * Get assignment and verify organization ownership.
         */
        OrderItemAssignmentEntity assignment =
                getAssignment(
                        organizationId,
                        assignmentId
                );

        /*
         * Normal production can only be submitted while
         * the assignment is in progress.
         */
        if (assignment.getStatus()
                != OrderItemAssignmentStatus.IN_PROGRESS) {

            throw new RuntimeException(
                    "Production can only be submitted for work in progress."
            );
        }

        /*
         * Verify that the current user belongs to
         * the assigned worker.
         *
         * Supports:
         * 1. Individual worker
         * 2. Member organization
         */
        validateWorkerAccess(
                assignment,
                currentMember
        );

        BigDecimal submittedQuantity =
                request.getSubmittedQuantity();

        /*
         * Get all previous submissions.
         */
        List<ProductionSubmissionEntity> existingSubmissions =
                submissionRepository
                        .findAllByAssignmentIdOrderBySubmittedAtAsc(
                                assignmentId
                        );

        /*
         * Only ORIGINAL submissions are counted here.
         *
         * Rework submissions are linked through reworkOf
         * and must NOT consume the original assignment quantity.
         */
        BigDecimal alreadySubmitted =
                existingSubmissions
                        .stream()
                        .filter(submission ->
                                submission.getReworkOf() == null
                        )
                        .map(
                                ProductionSubmissionEntity
                                        ::getSubmittedQuantity
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        /*
         * Remaining quantity that has never been submitted
         * as original production.
         */
        BigDecimal remainingOriginalQuantity =
                assignment.getAssignedQuantity()
                        .subtract(alreadySubmitted)
                        .max(BigDecimal.ZERO);

        /*
         * Never allow normal production to exceed the
         * remaining original quantity.
         */
        if (submittedQuantity.compareTo(
                remainingOriginalQuantity) > 0) {

            throw new RuntimeException(
                    "Production quantity exceeds remaining assigned quantity. "
                            + "Remaining quantity: "
                            + remainingOriginalQuantity
            );
        }

        /*
         * Resolve the worker rate and store it as a
         * historical snapshot on this submission.
         */
        BigDecimal workerRate =
                resolveWorkerRate(assignment);

        /*
         * Create a completely new submission.
         *
         * Previous submissions are never modified.
         */
        ProductionSubmissionEntity submission =
                ProductionSubmissionEntity.builder()
                        .assignment(assignment)
                        .worker(assignment.getWorker())
                        .submittedBy(currentUser)
                        .submittedQuantity(submittedQuantity)
                        .acceptedQuantity(BigDecimal.ZERO)
                        .rejectedQuantity(BigDecimal.ZERO)
                        .workerRate(workerRate)
                        .submittedAt(LocalDateTime.now())
                        .status(
                                ProductionSubmissionStatus.SUBMITTED
                        )
                        .submissionNotes(
                                request.getSubmissionNotes()
                        )
                        .build();

        submission =
                submissionRepository.save(submission);
        createWorkerEarning(submission);

        /*
         * Calculate original production submitted
         * after this submission.
         */
        BigDecimal newSubmittedQuantity =
                alreadySubmitted.add(
                        submittedQuantity
                );

        String message;

        if (newSubmittedQuantity.compareTo(
                assignment.getAssignedQuantity()) == 0) {

            message =
                    "Production submitted successfully. "
                            + "All assigned quantity has been submitted "
                            + "and is awaiting review.";

        } else {

            message =
                    "Production submitted successfully.";
        }

        return ApiResponse
                .<ProductionSubmissionResponse>builder()
                .success(true)
                .message(message)
                .data(toResponse(submission))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ProductionSubmissionResponse>>
    getProductionHistory(
            Long organizationId,
            Long assignmentId) {

        /*
         * Verify that the current user is an active
         * member of this organization.
         */
        UserEntity currentUser =
                securityUtils.getCurrentUser();

        getCurrentOrganizationMember(
                organizationId,
                currentUser.getId()
        );

        /*
         * Verify assignment ownership.
         */
        getAssignment(
                organizationId,
                assignmentId
        );

        List<ProductionSubmissionResponse> history =
                submissionRepository
                        .findAllByAssignmentIdOrderBySubmittedAtAsc(
                                assignmentId
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse
                .<List<ProductionSubmissionResponse>>builder()
                .success(true)
                .message(
                        "Production history fetched successfully."
                )
                .data(history)
                .build();
    }

    @Override
    public ApiResponse<ProductionSubmissionResponse> reviewProduction(
            Long organizationId,
            Long assignmentId,
            Long submissionId,
            ProductionReviewRequest request

            ) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();

        /*
         * Reviewer must be an active member of the organization.
         */
//        getCurrentOrganizationMember(
//                organizationId,
//                currentUser.getId()
//        );
        OrganizationMemberEntity reviewer =
                getCurrentOrganizationMember(
                        organizationId,
                        currentUser.getId()
                );

        validateProductionReviewer(reviewer);
        /*
         * Find submission and verify that it belongs
         * to this organization through its assignment.
         */
        ProductionSubmissionEntity submission =
                submissionRepository
                        .findByIdAndAssignmentIdAndAssignmentOrderItemOrderOrganizationId(
                                submissionId,
                                assignmentId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Production submission not found."
                                ));

        /*
         * A submission can only be reviewed once.
         */
        if (submission.getStatus()
                != ProductionSubmissionStatus.SUBMITTED) {

            throw new RuntimeException(
                    "Only submitted production can be reviewed."
            );
        }

        BigDecimal submittedQuantity =
                submission.getSubmittedQuantity();

        BigDecimal acceptedQuantity =
                request.getAcceptedQuantity();

        BigDecimal rejectedQuantity =
                request.getRejectedQuantity();

        /*
         * Accepted + rejected must exactly equal
         * the submitted quantity.
         */
        BigDecimal processedQuantity =
                acceptedQuantity.add(rejectedQuantity);

        if (processedQuantity.compareTo(
                submittedQuantity) != 0) {

            throw new RuntimeException(
                    "Accepted and rejected quantity must equal submitted quantity."
            );
        }

        /*
         * Determine the submission review status.
         */
        ProductionSubmissionStatus status =
                determineReviewStatus(
                        submittedQuantity,
                        acceptedQuantity,
                        rejectedQuantity
                );

        /*
         * Update review information.
         */
        submission.setAcceptedQuantity(
                acceptedQuantity
        );

        submission.setRejectedQuantity(
                rejectedQuantity
        );

        submission.setStatus(status);

        submission.setReviewedBy(
                currentUser
        );

        submission.setReviewedAt(
                LocalDateTime.now()
        );

        submission.setReviewNotes(
                request.getReviewNotes()
        );

        submission =
                submissionRepository.save(submission);

        /*
         * Get the assignment.
         */
        OrderItemAssignmentEntity assignment =
                submission.getAssignment();

        /*
         * Get complete production history for this assignment.
         */
        List<ProductionSubmissionEntity> submissions =
                submissionRepository
                        .findAllByAssignmentIdOrderBySubmittedAtAsc(
                                assignment.getId()
                        );

        /*
         * Calculate total quantity accepted across
         * all production submissions, including rework.
         */
        BigDecimal totalAccepted =
                submissions.stream()
                        .map(
                                ProductionSubmissionEntity
                                        ::getAcceptedQuantity
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        /*
         * Calculate rejected quantity that is still
         * waiting for rework.
         *
         * This handles:
         *
         * Original rejection
         *      ↓
         * Rework
         *      ↓
         * Rework rejected again
         *      ↓
         * Another rework
         */
        BigDecimal pendingReworkQuantity =
                calculatePendingReworkQuantity(
                        submissions
                );

        /*
         * Assignment is completed only when:
         *
         * 1. Required assigned quantity has been accepted.
         * 2. No rejected quantity is waiting for rework.
         */
        if (totalAccepted.compareTo(
                assignment.getAssignedQuantity()) == 0
                && pendingReworkQuantity.compareTo(
                BigDecimal.ZERO) == 0) {

            assignment.setStatus(
                    OrderItemAssignmentStatus.COMPLETED
            );

            assignment.setCompletedAt(
                    LocalDateTime.now()
            );

        } else {

            /*
             * There is still production or rework remaining.
             */
            assignment.setStatus(
                    OrderItemAssignmentStatus.IN_PROGRESS
            );

            assignment.setCompletedAt(null);
        }

        assignmentRepository.save(assignment);

        return ApiResponse
                .<ProductionSubmissionResponse>builder()
                .success(true)
                .message(
                        "Production reviewed successfully."
                )
                .data(
                        toResponse(submission)
                )
                .build();
    }


    @Override
    public ApiResponse<ProductionSubmissionResponse> submitRework(
            Long organizationId,
            Long assignmentId,
            Long rejectedSubmissionId,
            ProductionSubmissionRequest request) {

        UserEntity currentUser =
                securityUtils.getCurrentUser();

        OrganizationMemberEntity currentMember =
                getCurrentOrganizationMember(
                        organizationId,
                        currentUser.getId()
                );

        /*
         * Find the submission whose rejected quantity
         * is going to be reworked.
         */
        ProductionSubmissionEntity originalSubmission =
                submissionRepository
                        .findByIdAndAssignmentOrderItemOrderOrganizationId(
                                rejectedSubmissionId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Original submission not found."
                                ));

        /*
         * Rework cannot be created until the original
         * submission has been reviewed.
         */
        if (originalSubmission.getStatus()
                == ProductionSubmissionStatus.SUBMITTED) {

            throw new RuntimeException(
                    "Production must be reviewed before rework can be submitted."
            );
        }

        /*
         * Assignment must still be in progress.
         */
        OrderItemAssignmentEntity assignment =
                originalSubmission.getAssignment();

        if (assignment.getStatus()
                != OrderItemAssignmentStatus.IN_PROGRESS) {

            throw new RuntimeException(
                    "Rework cannot be submitted for a completed assignment."
            );
        }

        /*
         * The current user must belong to the assigned worker.
         */
        validateWorkerAccess(
                assignment,
                currentMember
        );

        BigDecimal rejectedQuantity =
                originalSubmission.getRejectedQuantity();

        if (rejectedQuantity == null
                || rejectedQuantity.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "This submission has no rejected quantity available for rework."
            );
        }

        /*
         * Get all submissions for this assignment.
         */
        List<ProductionSubmissionEntity> submissions =
                submissionRepository
                        .findAllByAssignmentIdOrderBySubmittedAtAsc(
                                assignment.getId()
                        );

        /*
         * Calculate how much rework has already been
         * submitted specifically for this submission.
         */
        BigDecimal alreadyReworked =
                submissions.stream()
                        .filter(submission ->
                                submission.getReworkOf() != null
                                        && submission
                                        .getReworkOf()
                                        .getId()
                                        .equals(
                                                originalSubmission.getId()
                                        )
                        )
                        .map(
                                ProductionSubmissionEntity
                                        ::getSubmittedQuantity
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        /*
         * Remaining rejected quantity for THIS submission.
         */
        BigDecimal remainingRework =
                rejectedQuantity
                        .subtract(alreadyReworked)
                        .max(BigDecimal.ZERO);

        /*
         * Do not allow the worker to submit more rework
         * than the rejected quantity of this submission.
         */
        if (request.getSubmittedQuantity()
                .compareTo(remainingRework) > 0) {

            throw new RuntimeException(
                    "Rework quantity cannot exceed remaining rejected quantity. "
                            + "Remaining rework quantity: "
                            + remainingRework
            );
        }

        /*
         * Resolve the worker rate at the time this
         * rework is submitted.
         */
        BigDecimal workerRate =
                resolveWorkerRate(assignment);

        /*
         * Create a completely new submission.
         *
         * The original submission remains unchanged.
         */
        ProductionSubmissionEntity reworkSubmission =
                ProductionSubmissionEntity.builder()
                        .assignment(assignment)
                        .worker(assignment.getWorker())
                        .submittedBy(currentUser)
                        .submittedQuantity(
                                request.getSubmittedQuantity()
                        )
                        .acceptedQuantity(
                                BigDecimal.ZERO
                        )
                        .rejectedQuantity(
                                BigDecimal.ZERO
                        )
                        .workerRate(workerRate)
                        .submittedAt(
                                LocalDateTime.now()
                        )
                        .status(
                                ProductionSubmissionStatus.SUBMITTED
                        )
                        .submissionNotes(
                                request.getSubmissionNotes()
                        )
                        .reworkOf(
                                originalSubmission
                        )
                        .build();

        reworkSubmission =
                submissionRepository.save(
                        reworkSubmission
                );

        return ApiResponse
                .<ProductionSubmissionResponse>builder()
                .success(true)
                .message(
                        "Rework submitted successfully."
                )
                .data(
                        toResponse(reworkSubmission)
                )
                .build();
    }
    /**
     * Resolve the worker rate applicable to this assignment.
     *
     * IMPORTANT:
     * The resolved rate is copied into ProductionSubmissionEntity
     * and therefore becomes a historical snapshot.
     */
    private BigDecimal resolveWorkerRate(
            OrderItemAssignmentEntity assignment) {

        Long workerId =
                assignment.getWorker().getId();

        Long productId =
                assignment.getOrderItem()
                        .getProduct()
                        .getId();

        Long organizationId =
                assignment.getOrderItem()
                        .getOrder()
                        .getOrganization()
                        .getId();

        /*
         * First look for a worker-specific rate.
         */
        return workerProductRateRepository
                .findByWorkerIdAndProductId(
                        workerId,
                        productId
                )
                .filter(WorkerProductRateEntity::getActive)
                .map(WorkerProductRateEntity::getRate)
                .orElseGet(() ->
                        getStandardWorkerRate(
                                organizationId,
                                productId
                        )
                );
    }

    /**
     * Verify that the logged-in user is actually allowed
     * to submit production for this worker.
     */
    private void validateWorkerAccess(
            OrderItemAssignmentEntity assignment,
            OrganizationMemberEntity currentMember) {

        OrganizationMemberEntity worker =
                assignment.getWorker();

        /*
         * Case 1:
         * Assignment belongs directly to this user.
         */
        if (worker.getUser() != null) {

            if (!worker.getId()
                    .equals(currentMember.getId())) {

                throw new RuntimeException(
                        "You are not assigned to this work."
                );
            }

            return;
        }

        /*
         * Case 2:
         * Assignment belongs to another organization.
         *
         * The logged-in user must be an active member
         * of that worker organization.
         */
        if (worker.getMemberOrganization() != null) {

            if (!currentMember.getOrganization()
                    .getId()
                    .equals(
                            worker.getMemberOrganization().getId()
                    )) {

                throw new RuntimeException(
                        "You are not authorized to submit production for this worker."
                );
            }

            return;
        }

        throw new RuntimeException(
                "Invalid worker configuration."
        );
    }

    private OrganizationMemberEntity
    getCurrentOrganizationMember(
            Long organizationId,
            Long userId) {

        return organizationMemberRepository
                .findByOrganizationIdAndUserIdAndStatus(
                        organizationId,
                        userId,
                        MembershipStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "You are not an active member of this organization."
                        )
                );
    }

    private OrderItemAssignmentEntity getAssignment(
            Long organizationId,
            Long assignmentId) {

        return assignmentRepository
                .findByIdAndOrderItemOrderOrganizationId(
                        assignmentId,
                        organizationId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Assignment not found."
                        )
                );
    }

    private ProductionSubmissionResponse toResponse(
            ProductionSubmissionEntity submission) {

        BigDecimal processedQuantity =
                submission.getAcceptedQuantity()
                        .add(
                                submission.getRejectedQuantity()
                        );

        String workerName;

        if (submission.getWorker().getUser() != null) {

            workerName =
                    submission.getWorker()
                            .getUser()
                            .getName();

        } else if (
                submission.getWorker()
                        .getMemberOrganization() != null) {

            workerName =
                    submission.getWorker()
                            .getMemberOrganization()
                            .getOrganizationName();

        } else {

            workerName = "Unknown Worker";
        }

        return ProductionSubmissionResponse
                .builder()
                .id(submission.getId())
                .assignmentId(
                        submission.getAssignment().getId()
                )
                .workerId(
                        submission.getWorker().getId()
                )
                .workerName(workerName)
                .submittedQuantity(
                        submission.getSubmittedQuantity()
                )
                .acceptedQuantity(
                        submission.getAcceptedQuantity()
                )
                .rejectedQuantity(
                        submission.getRejectedQuantity()
                )
                .processedQuantity(processedQuantity)
                .workerRate(
                        submission.getWorkerRate()
                )
                .status(
                        submission.getStatus()
                )
                .submissionNotes(
                        submission.getSubmissionNotes()
                )
                .submittedAt(
                        submission.getSubmittedAt()
                )
                .reviewedAt(
                        submission.getReviewedAt()
                )
                .build();
    }

    private BigDecimal getStandardWorkerRate(
            Long organizationId,
            Long productId) {

        ProductPricingEntity pricing =
                productPricingRepository
                        .findByOrganizationIdAndProductIdAndActiveTrue(
                                organizationId,
                                productId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Worker rate is not configured for this product."
                                )
                        );

        return pricing.getWorkerRate();
    }

    private ProductionSubmissionStatus determineReviewStatus(
            BigDecimal submittedQuantity,
            BigDecimal acceptedQuantity,
            BigDecimal rejectedQuantity) {

        if (acceptedQuantity.compareTo(BigDecimal.ZERO) == 0
                && rejectedQuantity.compareTo(submittedQuantity) == 0) {

            return ProductionSubmissionStatus.REJECTED;
        }

        if (acceptedQuantity.compareTo(submittedQuantity) == 0
                && rejectedQuantity.compareTo(BigDecimal.ZERO) == 0) {

            return ProductionSubmissionStatus.ACCEPTED;
        }

        return ProductionSubmissionStatus.PARTIALLY_ACCEPTED;
    }
    private BigDecimal calculateRemainingQuantity(
            OrderItemAssignmentEntity assignment) {

        List<ProductionSubmissionEntity> submissions =
                submissionRepository
                        .findAllByAssignmentIdOrderBySubmittedAtAsc(
                                assignment.getId()
                        );

        BigDecimal totalAccepted =
                submissions.stream()
                        .map(
                                ProductionSubmissionEntity
                                        ::getAcceptedQuantity
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return assignment.getAssignedQuantity()
                .subtract(totalAccepted)
                .max(BigDecimal.ZERO);
    }

    private BigDecimal calculatePendingReworkQuantity(
            List<ProductionSubmissionEntity> submissions) {

        BigDecimal pendingRework =
                BigDecimal.ZERO;

        for (ProductionSubmissionEntity submission : submissions) {

            /*
             * Every rejected quantity creates work that
             * needs to be reworked.
             */
            if (submission.getRejectedQuantity() != null
                    && submission.getRejectedQuantity()
                    .compareTo(BigDecimal.ZERO) > 0) {

                pendingRework =
                        pendingRework.add(
                                submission.getRejectedQuantity()
                        );
            }

            /*
             * Every rework submission consumes part of the
             * previously rejected quantity.
             */
            if (submission.getReworkOf() != null
                    && submission.getSubmittedQuantity() != null) {

                pendingRework =
                        pendingRework.subtract(
                                submission.getSubmittedQuantity()
                        );
            }
        }

        return pendingRework.max(BigDecimal.ZERO);
    }

    private void createWorkerEarning(
            ProductionSubmissionEntity submission) {

        if (submission.getAcceptedQuantity()
                .compareTo(BigDecimal.ZERO) <= 0) {

            return;
        }

        if (workerEarningRepository
                .existsBySubmissionId(submission.getId())) {

            return;
        }

        BigDecimal earningAmount =
                submission.getAcceptedQuantity()
                        .multiply(
                                submission.getWorkerRate()
                        );

        WorkerEarningEntity earning =
                WorkerEarningEntity.builder()
                        .organization(
                                submission.getAssignment()
                                        .getOrderItem()
                                        .getOrder()
                                        .getOrganization()
                        )
                        .worker(
                                submission.getWorker()
                        )
                        .submission(submission)
                        .acceptedQuantity(
                                submission.getAcceptedQuantity()
                        )
                        .workerRate(
                                submission.getWorkerRate()
                        )
                        .earningAmount(
                                earningAmount
                        )
                        .build();

        workerEarningRepository.save(earning);
    }

    private void validateProductionReviewer(
            OrganizationMemberEntity member) {

        OrganizationRole role =
                member.getRole();

        if (role != OrganizationRole.OWNER
                && role != OrganizationRole.ADMIN
                && role != OrganizationRole.MANAGER) {

            throw new RuntimeException(
                    "You are not authorized to review production."
            );
        }
    }
}