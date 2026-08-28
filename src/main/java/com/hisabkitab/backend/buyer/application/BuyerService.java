package com.hisabkitab.backend.buyer.application;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.buyer.interfaces.BuyerRequest;
import com.hisabkitab.backend.buyer.interfaces.BuyerResponse;

import java.util.List;

public interface BuyerService {

    ApiResponse<BuyerResponse> createBuyer(
            Long organizationId,
            Long customerId,
            BuyerRequest request
    );

    ApiResponse<List<BuyerResponse>> getBuyers(
            Long organizationId,
            Long customerId
    );

    ApiResponse<BuyerResponse> getBuyer(
            Long organizationId,
            Long customerId,
            Long buyerId
    );

    ApiResponse<BuyerResponse> updateBuyer(
            Long organizationId,
            Long customerId,
            Long buyerId,
            BuyerRequest request
    );

    ApiResponse<String> deactivateBuyer(
            Long organizationId,
            Long customerId,
            Long buyerId
    );
}