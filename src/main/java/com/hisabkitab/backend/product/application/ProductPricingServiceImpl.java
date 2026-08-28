package com.hisabkitab.backend.product.application;

import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.product.domain.ProductEntity;
import com.hisabkitab.backend.product.domain.ProductPricingEntity;
import com.hisabkitab.backend.product.infrastructure.ProductPricingRepository;
import com.hisabkitab.backend.product.infrastructure.ProductRepository;
import com.hisabkitab.backend.product.interfaces.dto.ProductPricingRequest;
import com.hisabkitab.backend.product.interfaces.dto.ProductPricingResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductPricingServiceImpl
        implements ProductPricingService {

    private final ProductPricingRepository productPricingRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ApiResponse<ProductPricingResponse> createPricing(
            Long organizationId,
            Long productId,
            ProductPricingRequest request) {

        securityUtils.getCurrentUser();

        ProductEntity product =
                productRepository
                        .findByIdAndOrganizationId(
                                productId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found."
                                ));

        if (productPricingRepository
                .findByOrganizationIdAndProductId(
                        organizationId,
                        productId
                )
                .isPresent()) {

            throw new RuntimeException(
                    "Pricing already exists for this product."
            );
        }

        OrganizationEntity organization =
                product.getOrganization();

        ProductPricingEntity pricing =
                ProductPricingEntity.builder()
                        .organization(organization)
                        .product(product)
                        .customerRate(
                                request.getCustomerRate()
                        )
                        .workerRate(
                                request.getWorkerRate()
                        )
                        .active(true)
                        .build();

        pricing =
                productPricingRepository.save(pricing);

        return ApiResponse
                .<ProductPricingResponse>builder()
                .success(true)
                .message(
                        "Product pricing created successfully."
                )
                .data(toResponse(pricing))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ProductPricingResponse> getPricing(
            Long organizationId,
            Long productId) {

        securityUtils.getCurrentUser();

        ProductPricingEntity pricing =
                productPricingRepository
                        .findByOrganizationIdAndProductId(
                                organizationId,
                                productId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product pricing not found."
                                ));

        return ApiResponse
                .<ProductPricingResponse>builder()
                .success(true)
                .message(
                        "Product pricing fetched successfully."
                )
                .data(toResponse(pricing))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<ProductPricingResponse> updatePricing(
            Long organizationId,
            Long productId,
            ProductPricingRequest request) {

        securityUtils.getCurrentUser();

        ProductPricingEntity pricing =
                productPricingRepository
                        .findByOrganizationIdAndProductId(
                                organizationId,
                                productId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product pricing not found."
                                ));

        pricing.setCustomerRate(
                request.getCustomerRate()
        );

        pricing.setWorkerRate(
                request.getWorkerRate()
        );

        pricing =
                productPricingRepository.save(pricing);

        return ApiResponse
                .<ProductPricingResponse>builder()
                .success(true)
                .message(
                        "Product pricing updated successfully."
                )
                .data(toResponse(pricing))
                .build();
    }

    private ProductPricingResponse toResponse(
            ProductPricingEntity pricing) {

        return ProductPricingResponse.builder()
                .id(pricing.getId())
                .organizationId(
                        pricing.getOrganization().getId()
                )
                .productId(
                        pricing.getProduct().getId()
                )
                .productName(
                        pricing.getProduct().getName()
                )
                .customerRate(
                        pricing.getCustomerRate()
                )
                .workerRate(
                        pricing.getWorkerRate()
                )
                .active(pricing.getActive())
                .createdAt(pricing.getCreatedAt())
                .updatedAt(pricing.getUpdatedAt())
                .build();
    }
}