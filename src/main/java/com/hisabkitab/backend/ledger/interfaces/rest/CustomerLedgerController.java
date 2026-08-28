package com.hisabkitab.backend.ledger.interfaces.rest;

import com.hisabkitab.backend.ledger.application.CustomerLedgerService;
import com.hisabkitab.backend.ledger.interfaces.dto.CustomerLedgerEntryResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations/{organizationId}")
@RequiredArgsConstructor
public class CustomerLedgerController {

    private final CustomerLedgerService customerLedgerService;

    @GetMapping("/customers/{customerId}/ledger")
    public ResponseEntity<
            ApiResponse<List<CustomerLedgerEntryResponse>>>
    getCustomerLedger(
            @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                customerLedgerService.getCustomerLedger(
                        organizationId,
                        customerId
                )
        );
    }
}