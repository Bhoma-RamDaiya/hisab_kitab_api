package com.hisabkitab.backend.worker.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerPaymentRequest {

    @NotNull(message = "Amount is required.")
    @Positive(message = "Payment amount must be greater than zero.")
    private BigDecimal amount;

    private LocalDateTime paidAt;

    private String paymentReference;

    private String remarks;
}