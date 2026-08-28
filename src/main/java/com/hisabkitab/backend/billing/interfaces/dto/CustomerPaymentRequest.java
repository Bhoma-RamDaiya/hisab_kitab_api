package com.hisabkitab.backend.billing.interfaces.dto;

import com.hisabkitab.backend.payment.interfaces.dto.PaymentAllocationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CustomerPaymentRequest {

    @NotNull(message = "Payment amount is required.")
    @Positive(message = "Payment amount must be greater than zero.")
    private BigDecimal amount;

    private LocalDateTime paidAt;

    private String paymentReference;

    private String remarks;

    /*
     * Used only when organization's allocation method
     * is MANUAL.
     */
    @Valid
    @Builder.Default
    private List<PaymentAllocationRequest> allocations =
            new ArrayList<>();
}