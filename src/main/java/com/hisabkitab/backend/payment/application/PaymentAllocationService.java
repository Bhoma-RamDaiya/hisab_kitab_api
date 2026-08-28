package com.hisabkitab.backend.payment.application;




import com.hisabkitab.backend.payment.interfaces.dto.PaymentAllocationRequest;
import com.hisabkitab.backend.payment.interfaces.dto.PaymentAllocationResponse;
import com.hisabkitab.backend.shared.response.ApiResponse;
import org.springframework.stereotype.Service;

import java.util.List;


public interface PaymentAllocationService {

    ApiResponse<PaymentAllocationResponse> allocatePayment(
            Long organizationId,
            Long paymentId,
            PaymentAllocationRequest request
    );

    ApiResponse<List<PaymentAllocationResponse>> getPaymentAllocations(
            Long organizationId,
            Long paymentId
    );

    ApiResponse<List<PaymentAllocationResponse>> getBillAllocations(
            Long organizationId,
            Long billId
    );
}