package com.hisabkitab.backend.product.application;

import com.hisabkitab.backend.product.interfaces.dto.*;
import com.hisabkitab.backend.shared.response.ApiResponse;

import java.util.List;

public interface ProductService {

    ApiResponse<MeasurementResponse> createMeasurement(
            Long organizationId,
            CreateMeasurementRequest request
    );

    ApiResponse<List<MeasurementResponse>> getMeasurements(
            Long organizationId
    );

    ApiResponse<ProductResponse> createProduct(
            Long organizationId,
            CreateProductRequest request
    );

    ApiResponse<List<ProductResponse>> getProducts(
            Long organizationId
    );

    ApiResponse<ProductResponse> getProduct(
            Long organizationId,
            Long productId
    );

    ApiResponse<ProductResponse> updateProduct(
            Long organizationId,
            Long productId,
            CreateProductRequest request
    );
}