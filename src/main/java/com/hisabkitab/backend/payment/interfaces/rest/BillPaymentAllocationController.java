package com.hisabkitab.backend.payment.interfaces.rest;

import com.hisabkitab.backend.payment.application.PaymentAllocationService;
import com.hisabkitab.backend.payment.interfaces.dto.PaymentAllocationResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/organizations/{organizationId}/bills"
)
@RequiredArgsConstructor
public class BillPaymentAllocationController {

//    private final PaymentAllocationService paymentAllocationService;

//    @GetMapping("/{billId}/allocations")
//    public ResponseEntity<ApiResponse<List<PaymentAllocationResponse>>>
//    getBillAllocations(
//            @PathVariable Long organizationId,
//            @PathVariable Long billId) {
//
//        return ResponseEntity.ok(
//                paymentAllocationService.getBillAllocations(
//                        organizationId,
//                        billId
//                )
//        );
//    }
}