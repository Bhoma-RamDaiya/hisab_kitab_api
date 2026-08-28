package com.hisabkitab.backend.product.application;

import com.hisabkitab.backend.product.interfaces.dto.ProductPricingRequest;
import com.hisabkitab.backend.product.interfaces.dto.ProductPricingResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;

public interface ProductPricingService {

    ApiResponse<ProductPricingResponse> createPricing(
            Long organizationId,
            Long productId,
            ProductPricingRequest request
    );

    ApiResponse<ProductPricingResponse> getPricing(
            Long organizationId,
            Long productId
    );

    ApiResponse<ProductPricingResponse> updatePricing(
            Long organizationId,
            Long productId,
            ProductPricingRequest request
    );
}