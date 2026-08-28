package com.hisabkitab.backend.worker.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerPayableResponse {

    private Long workerId;

    private String workerName;

    private BigDecimal totalEarned;

    private BigDecimal totalPaid;

    private BigDecimal pendingPayable;
}