package com.hisabkitab.backend.customer.application;

import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerRequest;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerResponse;

import java.util.List;

public interface CustomerService {

    ApiResponse<CustomerResponse> createCustomer(
            CustomerRequest request, Long organizationId
    );

    ApiResponse<CustomerResponse> getCustomer(
            Long organizationId,
            Long customerId
    );

    ApiResponse<List<CustomerResponse>> getCustomers(  Long organizationId);

    ApiResponse<CustomerResponse> updateCustomer(
            Long organizationId,
            Long customerId,
            CustomerRequest request
    );

    ApiResponse<String> deactivateCustomer(
            Long organizationId,
            Long customerId
    );
}