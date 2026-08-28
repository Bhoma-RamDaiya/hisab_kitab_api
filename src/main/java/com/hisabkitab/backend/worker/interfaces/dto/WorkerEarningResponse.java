package com.hisabkitab.backend.worker.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerEarningResponse {

    private Long id;

    private Long workerId;

    private String workerName;

    private Long submissionId;

    private Long assignmentId;

    private BigDecimal acceptedQuantity;

    private BigDecimal workerRate;

    private BigDecimal earningAmount;

    private LocalDateTime createdAt;
}