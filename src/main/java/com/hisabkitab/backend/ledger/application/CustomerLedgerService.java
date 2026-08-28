package com.hisabkitab.backend.ledger.application;

import com.hisabkitab.backend.ledger.interfaces.dto.CustomerLedgerEntryResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;

import java.util.List;

public interface CustomerLedgerService {

    ApiResponse<List<CustomerLedgerEntryResponse>> getCustomerLedger(
            Long organizationId,
            Long customerId
    );
}