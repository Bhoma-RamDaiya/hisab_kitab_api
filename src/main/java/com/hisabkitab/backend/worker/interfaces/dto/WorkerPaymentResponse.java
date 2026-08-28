package com.hisabkitab.backend.worker.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerPaymentResponse {

    private Long id;

    private Long workerId;

    private String workerName;

    private BigDecimal amount;

    private String paymentReference;

    private String remarks;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
}