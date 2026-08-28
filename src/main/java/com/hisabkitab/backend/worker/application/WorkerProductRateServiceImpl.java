package com.hisabkitab.backend.worker.application;

import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.product.domain.ProductEntity;
import com.hisabkitab.backend.product.infrastructure.ProductRepository;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.utils.SecurityUtils;
import com.hisabkitab.backend.worker.domain.WorkerProductRateEntity;
import com.hisabkitab.backend.worker.infrastructure.WorkerProductRateRepository;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerProductRateRequest;
import com.hisabkitab.backend.worker.interfaces.dto.WorkerProductRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkerProductRateServiceImpl
        implements WorkerProductRateService {

    private final WorkerProductRateRepository workerProductRateRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ApiResponse<WorkerProductRateResponse> createRate(
            Long organizationId,
            Long workerId,
            Long productId,
            WorkerProductRateRequest request) {

        securityUtils.getCurrentUser();

        OrganizationMemberEntity worker =
                getActiveWorker(
                        workerId,
                        organizationId
                );

        ProductEntity product =
                getOrganizationProduct(
                        productId,
                        organizationId
                );

        if (workerProductRateRepository
                .findByWorkerIdAndProductId(
                        workerId,
                        productId
                )
                .isPresent()) {

            throw new RuntimeException(
                    "Worker product rate already exists."
            );
        }

        WorkerProductRateEntity rate =
                WorkerProductRateEntity.builder()
                        .worker(worker)
                        .product(product)
                        .rate(request.getRate())
                        .active(true)
                        .build();

        rate =
                workerProductRateRepository.save(rate);

        return ApiResponse
                .<WorkerProductRateResponse>builder()
                .success(true)
                .message(
                        "Worker product rate created successfully."
                )
                .data(toResponse(rate))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<WorkerProductRateResponse> getRate(
            Long organizationId,
            Long workerId,
            Long productId) {

        securityUtils.getCurrentUser();

        getActiveWorker(
                workerId,
                organizationId
        );

        getOrganizationProduct(
                productId,
                organizationId
        );

        WorkerProductRateEntity rate =
                workerProductRateRepository
                        .findByWorkerIdAndProductId(
                                workerId,
                                productId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Worker product rate not found."
                                ));

        return ApiResponse
                .<WorkerProductRateResponse>builder()
                .success(true)
                .message(
                        "Worker product rate fetched successfully."
                )
                .data(toResponse(rate))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<WorkerProductRateResponse> updateRate(
            Long organizationId,
            Long workerId,
            Long productId,
            WorkerProductRateRequest request) {

        securityUtils.getCurrentUser();

        getActiveWorker(
                workerId,
                organizationId
        );

        getOrganizationProduct(
                productId,
                organizationId
        );

        WorkerProductRateEntity rate =
                workerProductRateRepository
                        .findByWorkerIdAndProductId(
                                workerId,
                                productId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Worker product rate not found."
                                ));

        rate.setRate(request.getRate());

        rate =
                workerProductRateRepository.save(rate);

        return ApiResponse
                .<WorkerProductRateResponse>builder()
                .success(true)
                .message(
                        "Worker product rate updated successfully."
                )
                .data(toResponse(rate))
                .build();
    }

    private OrganizationMemberEntity getActiveWorker(
            Long workerId,
            Long organizationId) {

        return organizationMemberRepository
                .findByIdAndOrganizationIdAndStatus(
                        workerId,
                        organizationId,
                        MembershipStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Worker is not an active member of this organization."
                        ));
    }

    private ProductEntity getOrganizationProduct(
            Long productId,
            Long organizationId) {

        return productRepository
                .findByIdAndOrganizationId(
                        productId,
                        organizationId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product does not belong to this organization."
                        ));
    }

    private WorkerProductRateResponse toResponse(
            WorkerProductRateEntity rate) {

        OrganizationMemberEntity worker =
                rate.getWorker();

        String workerName;

        if (worker.getUser() != null) {

            workerName =
                    worker.getUser().getName();

        } else if (
                worker.getMemberOrganization() != null) {

            workerName =
                    worker.getMemberOrganization().getOrganizationName();

        } else {

            workerName = "Unknown Worker";
        }

        return WorkerProductRateResponse.builder()
                .id(rate.getId())
                .workerId(worker.getId())
                .workerName(workerName)
                .productId(rate.getProduct().getId())
                .productName(rate.getProduct().getName())
                .rate(rate.getRate())
                .active(rate.getActive())
                .createdAt(rate.getCreatedAt())
                .updatedAt(rate.getUpdatedAt())
                .build();
    }
}