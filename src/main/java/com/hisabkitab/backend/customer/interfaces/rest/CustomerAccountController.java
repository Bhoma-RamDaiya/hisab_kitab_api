package com.hisabkitab.backend.customer.interfaces.rest;

import com.hisabkitab.backend.customer.application.CustomerAccountService;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerAccountResponse;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerStatementResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/organizations/{organizationId}/customers")
@RequiredArgsConstructor
public class CustomerAccountController {

    private final CustomerAccountService customerAccountService;

    @GetMapping("/{customerId}/account")
    public ResponseEntity<ApiResponse<CustomerAccountResponse>>
    getCustomerAccount(
            @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                customerAccountService.getCustomerAccount(
                        organizationId,
                        customerId
                )
        );
    }

    @GetMapping("/{customerId}/statement")
    public ResponseEntity<ApiResponse<CustomerStatementResponse>>
    getCustomerStatement(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate) {

        return ResponseEntity.ok(
                customerAccountService.getCustomerStatement(
                        organizationId,
                        customerId,
                        fromDate,
                        toDate
                )
        );
    }
}