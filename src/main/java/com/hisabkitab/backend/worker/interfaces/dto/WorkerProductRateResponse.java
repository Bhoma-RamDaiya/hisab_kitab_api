package com.hisabkitab.backend.worker.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProductRateResponse {

    private Long id;

    private Long workerId;

    private String workerName;

    private Long productId;

    private String productName;

    private BigDecimal rate;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}