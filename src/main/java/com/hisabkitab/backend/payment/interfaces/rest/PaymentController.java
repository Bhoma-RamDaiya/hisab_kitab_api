package com.hisabkitab.backend.payment.interfaces.rest;

import com.hisabkitab.backend.billing.application.CustomerPaymentService;
import com.hisabkitab.backend.billing.interfaces.dto.CustomerBalanceResponse;
import com.hisabkitab.backend.billing.interfaces.dto.CustomerPaymentRequest;
import com.hisabkitab.backend.billing.interfaces.dto.CustomerPaymentResponse;
import com.hisabkitab.backend.payment.application.CustomerCreditService;
import com.hisabkitab.backend.payment.interfaces.dto.CustomerCreditAllocationRequest;
import com.hisabkitab.backend.payment.interfaces.dto.CustomerCreditAllocationResponse;
import com.hisabkitab.backend.payment.interfaces.dto.CustomerCreditResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations/{organizationId}/customers/{customerId}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final CustomerPaymentService customerPaymentService;
    private final CustomerCreditService customerCreditService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerPaymentResponse>> createCustomerPayment(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerPaymentRequest request) {

        return ResponseEntity.ok(
                customerPaymentService.createPayment(
                        organizationId,
                        customerId,
                        request
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerPaymentResponse>>> getCustomerPayments(
            @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                customerPaymentService.getCustomerPayments(
                        organizationId,
                        customerId
                )
        );
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<CustomerBalanceResponse>> getCustomerBalance(
            @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                customerPaymentService.getCustomerBalance(
                        organizationId,
                        customerId
                )
        );
    }

    @PostMapping("/credit/apply/{billId}")
    public ResponseEntity<ApiResponse<List<CustomerCreditAllocationResponse>>> applyCreditToBill(
            @PathVariable Long organizationId,
            @PathVariable Long customerId,
            @PathVariable Long billId,
            @Valid @RequestBody CustomerCreditAllocationRequest request) {

        return ResponseEntity.ok(
                customerCreditService.applyCreditToBill(
                        organizationId,
                        customerId,
                        billId,
                        request
                )
        );
    }

    @GetMapping("/credit")
    public ResponseEntity<ApiResponse<CustomerCreditResponse>> getCustomerCredit(
            @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                customerCreditService.getCustomerCredit(
                        organizationId,
                        customerId
                )
        );
    }

    @GetMapping("/credit/history")
    public ResponseEntity<ApiResponse<List<CustomerCreditAllocationResponse>>> getCustomerCreditHistory(
            @PathVariable Long organizationId,
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                customerCreditService.getCustomerCreditHistory(
                        organizationId,
                        customerId
                )
        );
    }
}