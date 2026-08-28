package com.hisabkitab.backend.payment.interfaces.dto;

import com.hisabkitab.backend.payment.domain.PaymentMethod;
import com.hisabkitab.backend.payment.interfaces.dto.PaymentAllocationResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;

    private Long organizationId;

    private Long customerId;

    private Long payerId;

    private String payerName;

    private BigDecimal amount;

    private LocalDateTime paymentDate;

    private PaymentMethod paymentMethod;

    private String referenceNumber;

    private String notes;

    private List<PaymentAllocationResponse> allocations;

    private BigDecimal allocatedAmount;

    private BigDecimal unallocatedAmount;

    private LocalDateTime createdAt;
}