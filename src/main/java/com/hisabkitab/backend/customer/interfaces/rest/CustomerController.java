package com.hisabkitab.backend.customer.interfaces.rest;



import com.hisabkitab.backend.customer.interfaces.dto.CustomerRequest;
import com.hisabkitab.backend.customer.interfaces.dto.CustomerResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import com.hisabkitab.backend.customer.application.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations/{organizationId}/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @PathVariable Long organizationId,
            @Valid @RequestBody CustomerRequest request) {

        return ResponseEntity.ok(
                customerService.createCustomer(request , organizationId)
        );
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(
            @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                customerService.getCustomer(organizationId,customerId)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getCustomers( @PathVariable Long organizationId) {

        return ResponseEntity.ok(
                customerService.getCustomers(organizationId)
        );
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerRequest request) {

        return ResponseEntity.ok(
                customerService.updateCustomer(
                        organizationId,
                        customerId,
                        request
                )
        );
    }

    @PatchMapping("/{customerId}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateCustomer(
           @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                customerService.deactivateCustomer(organizationId ,customerId)
        );
    }
}
