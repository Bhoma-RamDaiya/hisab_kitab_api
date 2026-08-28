package com.hisabkitab.backend.billing.application;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.billing.interfaces.dto.BillRequest;
import com.hisabkitab.backend.billing.interfaces.dto.BillResponse;

import java.util.List;

public interface BillService {

    ApiResponse<BillResponse> createBill(
            Long organizationId,
            Long customerId,
            BillRequest request
    );

    ApiResponse<List<BillResponse>> getBills(
            Long organizationId,
            Long customerId
    );

    ApiResponse<BillResponse> getBill(
            Long organizationId,
            Long customerId,
            Long billId
    );

    ApiResponse<String> cancelBill(
            Long organizationId,
            Long customerId,
            Long billId
    );
}