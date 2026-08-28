package com.hisabkitab.backend.customer.application;

import com.hisabkitab.backend.customer.interfaces.dto.CustomerAccountResponse;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerStatementResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface CustomerAccountService {

    ApiResponse<CustomerAccountResponse> getCustomerAccount(
            Long organizationId,
            Long customerId
    );

    ApiResponse<CustomerStatementResponse> getCustomerStatement(
            Long organizationId,
            Long customerId,
            LocalDate fromDate,
            LocalDate toDate
    );


}