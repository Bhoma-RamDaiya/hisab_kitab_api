package com.hisabkitab.backend.billing.interfaces.dto;

import com.hisabkitab.backend.payment.interfaces.dto.PaymentAllocationResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPaymentResponse {

    private Long id;

    private Long customerId;

    private String customerName;

    private BigDecimal amount;

    private LocalDateTime paidAt;

    private String paymentReference;

    private String remarks;

    /*
     * Total amount from this payment that was
     * allocated to bills.
     */
    private BigDecimal allocatedAmount;

    /*
     * Amount from this payment that became
     * customer credit.
     */
    private BigDecimal creditAmount;

    /*
     * Individual bill allocations.
     */
    @Builder.Default
    private List<PaymentAllocationResponse> allocations =
            new ArrayList<>();

    private LocalDateTime createdAt;
}