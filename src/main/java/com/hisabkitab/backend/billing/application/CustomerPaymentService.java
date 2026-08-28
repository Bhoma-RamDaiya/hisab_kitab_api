package com.hisabkitab.backend.billing.application;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.billing.interfaces.dto.*;

import java.util.List;

public interface CustomerPaymentService {

    ApiResponse<CustomerPaymentResponse> createPayment(
            Long organizationId,
            Long customerId,
            CustomerPaymentRequest request
    );

    ApiResponse<List<CustomerPaymentResponse>> getCustomerPayments(
            Long organizationId,
            Long customerId
    );

    ApiResponse<CustomerBalanceResponse> getCustomerBalance(
            Long organizationId,
            Long customerId
    );
}