package com.hisabkitab.backend.order.application;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.order.domain.OrderItemAssignmentEntity;
import com.hisabkitab.backend.order.domain.OrderItemAssignmentStatus;
import com.hisabkitab.backend.order.domain.OrderItemEntity;
import com.hisabkitab.backend.order.domain.ProductionSubmissionEntity;
import com.hisabkitab.backend.order.infrastructure.OrderItemAssignmentRepository;
import com.hisabkitab.backend.order.infrastructure.OrderItemRepository;
import com.hisabkitab.backend.order.infrastructure.ProductionSubmissionRepository;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemAssignmentRequest;
import com.hisabkitab.backend.order.interfaces.dto.OrderItemAssignmentResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.user.domain.UserEntity;
import com.hisabkitab.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemAssignmentServiceImpl
        implements OrderItemAssignmentService {

    private final OrderItemAssignmentRepository assignmentRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final SecurityUtils securityUtils;
    private final ProductionSubmissionRepository submissionRepository;

    @Override
    public ApiResponse<OrderItemAssignmentResponse> assignWorker(
            Long organizationId,
            Long orderItemId,
            OrderItemAssignmentRequest request) {

        getCurrentOrganizationMember(organizationId);

        OrderItemEntity orderItem =
                orderItemRepository
                        .findByIdAndOrderOrganizationId(
                                orderItemId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order item not found."
                                ));

        OrganizationMemberEntity worker =
                organizationMemberRepository
                        .findByIdAndOrganizationIdAndStatus(
                                request.getWorkerId(),
                                organizationId,
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Worker is not an active member of this organization."
                                ));

        BigDecimal alreadyAssigned =
                assignmentRepository
                        .findAllByOrderItemId(orderItemId)
                        .stream()
                        .filter(assignment ->
                                assignment.getStatus()
                                        != OrderItemAssignmentStatus.CANCELLED
                        )
                        .map(OrderItemAssignmentEntity::getAssignedQuantity)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal remaining =
                orderItem.getQuantity()
                        .subtract(alreadyAssigned);

        if (request.getAssignedQuantity()
                .compareTo(remaining) > 0) {

            throw new RuntimeException(
                    "Assigned quantity cannot exceed remaining quantity. "
                            + "Remaining quantity: "
                            + remaining
            );
        }

        OrderItemAssignmentEntity assignment =
                OrderItemAssignmentEntity.builder()
                        .orderItem(orderItem)
                        .worker(worker)
                        .assignedQuantity(
                                request.getAssignedQuantity()
                        )
                        .status(
                                OrderItemAssignmentStatus.ASSIGNED
                        )
                        .assignedAt(LocalDateTime.now())
                        .notes(request.getNotes())
                        .build();

        assignment =
                assignmentRepository.save(assignment);

        return ApiResponse
                .<OrderItemAssignmentResponse>builder()
                .success(true)
                .message("Work assigned successfully.")
                .data(toResponse(assignment))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrderItemAssignmentResponse>> getAssignments(
            Long organizationId,
            Long orderItemId) {

        getCurrentOrganizationMember(organizationId);

        orderItemRepository
                .findByIdAndOrderOrganizationId(
                        orderItemId,
                        organizationId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order item not found."
                        ));

        List<OrderItemAssignmentResponse> responses =
                assignmentRepository
                        .findAllByOrderItemId(orderItemId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse
                .<List<OrderItemAssignmentResponse>>builder()
                .success(true)
                .message("Assignments fetched successfully.")
                .data(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrderItemAssignmentResponse> getAssignment(
            Long organizationId,
            Long assignmentId) {

        getCurrentOrganizationMember(organizationId);

        OrderItemAssignmentEntity assignment =
                getAssignmentEntity(
                        organizationId,
                        assignmentId
                );

        return ApiResponse
                .<OrderItemAssignmentResponse>builder()
                .success(true)
                .message("Assignment fetched successfully.")
                .data(toResponse(assignment))
                .build();
    }

    @Override
    public ApiResponse<String> startAssignment(
            Long organizationId,
            Long assignmentId) {

        OrganizationMemberEntity currentMember =
                getCurrentOrganizationMember(organizationId);

        OrderItemAssignmentEntity assignment =
                getAssignmentEntity(
                        organizationId,
                        assignmentId
                );

        validateWorkerAccess(
                assignment,
                currentMember
        );

        if (assignment.getStatus()
                != OrderItemAssignmentStatus.ASSIGNED) {

            throw new RuntimeException(
                    "Only assigned work can be started."
            );
        }

        assignment.setStatus(
                OrderItemAssignmentStatus.IN_PROGRESS
        );

        assignment.setStartedAt(
                LocalDateTime.now()
        );

        assignmentRepository.save(assignment);

        return ApiResponse
                .<String>builder()
                .success(true)
                .message("Work started successfully.")
                .data("Work started successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrderItemAssignmentResponse>>
    getWorkerAssignments(
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

        List<OrderItemAssignmentResponse> responses =
                assignmentRepository
                        .findAllByWorkerIdAndOrderItemOrderOrganizationId(
                                workerId,
                                organizationId
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ApiResponse
                .<List<OrderItemAssignmentResponse>>builder()
                .success(true)
                .message("Worker assignments fetched successfully.")
                .data(responses)
                .build();
    }

    private OrganizationMemberEntity getCurrentOrganizationMember(
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

    private OrderItemAssignmentEntity getAssignmentEntity(
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
                        ));
    }

    /**
     * Makes sure that the currently logged-in user is allowed
     * to work on this assignment.
     *
     * Two cases are supported:
     *
     * 1. The worker is an individual user.
     * 2. The worker is another organization.
     */
    private void validateWorkerAccess(
            OrderItemAssignmentEntity assignment,
            OrganizationMemberEntity currentMember) {

        OrganizationMemberEntity worker =
                assignment.getWorker();

        /*
         * Individual worker.
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
         * Organization worker.
         *
         * Example:
         *
         * Organization A
         *      ↓
         * Organization B
         *
         * Any active user belonging to Organization B
         * can work on Organization B's assignment.
         */
        if (worker.getMemberOrganization() != null) {

            if (!currentMember.getOrganization()
                    .getId()
                    .equals(
                            worker.getMemberOrganization().getId()
                    )) {

                throw new RuntimeException(
                        "You are not authorized to work on this assignment."
                );
            }

            return;
        }

        throw new RuntimeException(
                "Invalid worker configuration."
        );
    }

    private OrderItemAssignmentResponse toResponse(
            OrderItemAssignmentEntity assignment) {

        List<ProductionSubmissionEntity> submissions =
                submissionRepository
                        .findAllByAssignmentIdOrderBySubmittedAtAsc(
                                assignment.getId()
                        );

        BigDecimal acceptedQuantity =
                submissions.stream()
                        .map(
                                ProductionSubmissionEntity
                                        ::getAcceptedQuantity
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal rejectedQuantity =
                submissions.stream()
                        .map(
                                ProductionSubmissionEntity
                                        ::getRejectedQuantity
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        /*
         * Total quantity still required to complete
         * the assignment.
         */
        BigDecimal remainingQuantity =
                assignment.getAssignedQuantity()
                        .subtract(acceptedQuantity)
                        .max(BigDecimal.ZERO);

        /*
         * Rejected quantity from original submissions
         * that has not yet been resubmitted as rework.
         */
        BigDecimal totalRejected =
                submissions.stream()
                        .filter(submission ->
                                submission.getReworkOf() == null
                        )
                        .map(
                                ProductionSubmissionEntity
                                        ::getRejectedQuantity
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalReworkSubmitted =
                submissions.stream()
                        .filter(submission ->
                                submission.getReworkOf() != null
                        )
                        .map(
                                ProductionSubmissionEntity
                                        ::getSubmittedQuantity
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal pendingReworkQuantity =
                totalRejected
                        .subtract(totalReworkSubmitted)
                        .max(BigDecimal.ZERO);

        String workerName;

        if (assignment.getWorker().getUser() != null) {

            workerName =
                    assignment.getWorker()
                            .getUser()
                            .getName();

        } else if (
                assignment.getWorker()
                        .getMemberOrganization() != null) {

            workerName =
                    assignment.getWorker()
                            .getMemberOrganization()
                            .getOrganizationName();

        } else {

            workerName = "Unknown Worker";
        }

        return OrderItemAssignmentResponse.builder()
                .id(assignment.getId())
                .orderItemId(
                        assignment.getOrderItem().getId()
                )
                .workerId(
                        assignment.getWorker().getId()
                )
                .workerName(workerName)
                .assignedQuantity(
                        assignment.getAssignedQuantity()
                )
                .completedQuantity(
                        acceptedQuantity
                )
                .rejectedQuantity(
                        rejectedQuantity
                )
                .pendingReworkQuantity(
                        pendingReworkQuantity
                )
                .remainingQuantity(
                        remainingQuantity
                )
                .status(
                        assignment.getStatus()
                )
                .assignedAt(
                        assignment.getAssignedAt()
                )
                .startedAt(
                        assignment.getStartedAt()
                )
                .completedAt(
                        assignment.getCompletedAt()
                )
                .notes(
                        assignment.getNotes()
                )
                .build();
    }
}