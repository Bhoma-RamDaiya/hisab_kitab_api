package com.hisabkitab.backend.product.application;


import com.hisabkitab.backend.membership.domain.MembershipStatus;
import com.hisabkitab.backend.membership.domain.OrganizationMemberEntity;
import com.hisabkitab.backend.membership.infrastructure.OrganizationMemberRepository;
import com.hisabkitab.backend.organization.domain.OrganizationEntity;
import com.hisabkitab.backend.product.domain.MeasurementDefinitionEntity;
import com.hisabkitab.backend.product.domain.ProductEntity;
import com.hisabkitab.backend.product.domain.ProductMeasurementEntity;
import com.hisabkitab.backend.product.infrastructure.MeasurementDefinitionRepository;
import com.hisabkitab.backend.product.infrastructure.ProductMeasurementRepository;
import com.hisabkitab.backend.product.infrastructure.ProductRepository;
import com.hisabkitab.backend.product.interfaces.dto.*;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final MeasurementDefinitionRepository measurementRepository;
    private final ProductMeasurementRepository productMeasurementRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final SecurityUtils securityUtils;

    // =========================================================
    // CREATE MEASUREMENT
    // =========================================================

    @Override
    public ApiResponse<MeasurementResponse> createMeasurement(
            Long organizationId,
            CreateMeasurementRequest request) {

        OrganizationEntity organization =
                getOrganization(organizationId);

        boolean exists =
                measurementRepository
                        .existsByOrganizationIdAndNameIgnoreCase(
                                organizationId,
                                request.getName().trim()
                        );

        if (exists) {
            throw new RuntimeException(
                    "Measurement with this name already exists."
            );
        }

        MeasurementDefinitionEntity measurement =
                MeasurementDefinitionEntity.builder()
                        .organization(organization)
                        .name(request.getName().trim())
                        .unit(request.getUnit())
                        .description(request.getDescription())
                        .active(true)
                        .build();

        measurement =
                measurementRepository.save(measurement);

        return ApiResponse.<MeasurementResponse>builder()
                .success(true)
                .message("Measurement created successfully.")
                .data(toMeasurementResponse(measurement))
                .build();
    }

    // =========================================================
    // GET MEASUREMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<MeasurementResponse>> getMeasurements(
            Long organizationId) {

        getOrganization(organizationId);

        List<MeasurementResponse> measurements =
                measurementRepository
                        .findAllByOrganizationIdAndActiveTrue(
                                organizationId
                        )
                        .stream()
                        .map(this::toMeasurementResponse)
                        .toList();

        return ApiResponse.<List<MeasurementResponse>>builder()
                .success(true)
                .message("Measurements fetched successfully.")
                .data(measurements)
                .build();
    }

    // =========================================================
    // CREATE PRODUCT
    // =========================================================

    @Override
    public ApiResponse<ProductResponse> createProduct(
            Long organizationId,
            CreateProductRequest request) {

        OrganizationEntity organization =
                getOrganization(organizationId);

        boolean exists =
                productRepository
                        .existsByOrganizationIdAndNameIgnoreCase(
                                organizationId,
                                request.getName().trim()
                        );

        if (exists) {
            throw new RuntimeException(
                    "Product with this name already exists."
            );
        }

        ProductEntity product =
                ProductEntity.builder()
                        .organization(organization)
                        .name(request.getName().trim())
                        .description(request.getDescription())
                        .active(true)
                        .measurements(new ArrayList<>())
                        .build();

        /*
         * Add selected measurements.
         */
        addMeasurementsToProduct(
                product,
                organizationId,
                request.getMeasurements()
        );

        product =
                productRepository.save(product);

        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product created successfully.")
                .data(toProductResponse(product))
                .build();
    }

    // =========================================================
    // GET PRODUCTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ProductResponse>> getProducts(
            Long organizationId) {

        getOrganization(organizationId);

        List<ProductResponse> products =
                productRepository
                        .findAllByOrganizationIdAndActiveTrue(
                                organizationId
                        )
                        .stream()
                        .map(this::toProductResponse)
                        .toList();

        return ApiResponse.<List<ProductResponse>>builder()
                .success(true)
                .message("Products fetched successfully.")
                .data(products)
                .build();
    }

    // =========================================================
    // GET PRODUCT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ProductResponse> getProduct(
            Long organizationId,
            Long productId) {

        ProductEntity product =
                getProductEntity(
                        organizationId,
                        productId
                );

        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product fetched successfully.")
                .data(toProductResponse(product))
                .build();
    }

    // =========================================================
    // UPDATE PRODUCT
    // =========================================================

    @Override
    public ApiResponse<ProductResponse> updateProduct(
            Long organizationId,
            Long productId,
            CreateProductRequest request) {

        ProductEntity product =
                getProductEntity(
                        organizationId,
                        productId
                );

        /*
         * Check duplicate name only when name changes.
         */
        if (!product.getName()
                .equalsIgnoreCase(request.getName().trim())) {

            boolean exists =
                    productRepository
                            .existsByOrganizationIdAndNameIgnoreCase(
                                    organizationId,
                                    request.getName().trim()
                            );

            if (exists) {
                throw new RuntimeException(
                        "Product with this name already exists."
                );
            }
        }

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());

        /*
         * Replace product measurements.
         *
         * Because ProductEntity uses orphanRemoval=true,
         * old ProductMeasurementEntity records will be removed.
         */
        product.getMeasurements().clear();

        addMeasurementsToProduct(
                product,
                organizationId,
                request.getMeasurements()
        );

        product =
                productRepository.save(product);

        return ApiResponse.<ProductResponse>builder()
                .success(true)
                .message("Product updated successfully.")
                .data(toProductResponse(product))
                .build();
    }

    // =========================================================
    // ADD PRODUCT MEASUREMENTS
    // =========================================================

    private void addMeasurementsToProduct(
            ProductEntity product,
            Long organizationId,
            List<ProductMeasurementRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            return;
        }

        for (ProductMeasurementRequest request : requests) {

            MeasurementDefinitionEntity measurement =
                    measurementRepository
                            .findByIdAndOrganizationId(
                                    request.getMeasurementId(),
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Measurement not found or does not belong to this organization."
                                    ));

            ProductMeasurementEntity productMeasurement =
                    ProductMeasurementEntity.builder()
                            .product(product)
                            .measurement(measurement)
                            .required(
                                    request.getRequired() != null
                                            ? request.getRequired()
                                            : true
                            )
                            .displayOrder(
                                    request.getDisplayOrder()
                            )
                            .build();

            product.getMeasurements()
                    .add(productMeasurement);
        }
    }

    // =========================================================
    // ORGANIZATION ACCESS
    // =========================================================

    private OrganizationEntity getOrganization(
            Long organizationId) {

        var currentUser =
                securityUtils.getCurrentUser();

        OrganizationMemberEntity member =
                organizationMemberRepository
                        .findByOrganizationIdAndUserIdAndStatus(
                                organizationId,
                                currentUser.getId(),
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "You are not an active member of this organization."
                                ));

        return member.getOrganization();
    }

    // =========================================================
    // PRODUCT
    // =========================================================

    private ProductEntity getProductEntity(
            Long organizationId,
            Long productId) {

        return productRepository
                .findByIdAndOrganizationId(
                        productId,
                        organizationId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found."
                        ));
    }

    // =========================================================
    // MAPPERS
    // =========================================================

    private MeasurementResponse toMeasurementResponse(
            MeasurementDefinitionEntity measurement) {

        return MeasurementResponse.builder()
                .id(measurement.getId())
                .organizationId(
                        measurement.getOrganization().getId()
                )
                .name(measurement.getName())
                .unit(measurement.getUnit())
                .description(measurement.getDescription())
                .active(measurement.getActive())
                .build();
    }

    private ProductResponse toProductResponse(
            ProductEntity product) {

        List<ProductMeasurementResponse> measurements =
                product.getMeasurements()
                        .stream()
                        .map(pm ->
                                ProductMeasurementResponse.builder()
                                        .id(pm.getId())
                                        .measurementId(
                                                pm.getMeasurement().getId()
                                        )
                                        .measurementName(
                                                pm.getMeasurement().getName()
                                        )
                                        .unit(
                                                pm.getMeasurement().getUnit()
                                        )
                                        .required(
                                                pm.getRequired()
                                        )
                                        .displayOrder(
                                                pm.getDisplayOrder()
                                        )
                                        .build()
                        )
                        .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .organizationId(
                        product.getOrganization().getId()
                )
                .name(product.getName())
                .description(product.getDescription())
                .active(product.getActive())
                .measurements(measurements)
                .build();
    }
}