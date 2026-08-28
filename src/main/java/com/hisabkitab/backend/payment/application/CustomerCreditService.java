package com.hisabkitab.backend.payment.application;

import com.hisabkitab.backend.payment.interfaces.dto.CustomerCreditAllocationRequest;
import com.hisabkitab.backend.payment.interfaces.dto.CustomerCreditAllocationResponse;
import com.hisabkitab.backend.payment.interfaces.dto.CustomerCreditResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;

import java.util.List;

public interface CustomerCreditService {

    ApiResponse<List<CustomerCreditAllocationResponse>> applyCreditToBill(
            Long organizationId,
            Long customerId,
            Long billId,
            CustomerCreditAllocationRequest request
    );

    ApiResponse<CustomerCreditResponse> getCustomerCredit(
            Long organizationId,
            Long customerId
    );

    ApiResponse<List<CustomerCreditAllocationResponse>>
    getCustomerCreditHistory(
            Long organizationId,
            Long customerId
    );
}