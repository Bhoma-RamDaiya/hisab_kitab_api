package com.hisabkitab.backend.order.interfaces.dto;

import com.hisabkitab.backend.order.domain.ProductionSubmissionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionSubmissionResponse {

    private Long id;

    private Long assignmentId;

    private Long workerId;

    private String workerName;

    private BigDecimal submittedQuantity;

    private BigDecimal acceptedQuantity;

    private BigDecimal rejectedQuantity;

    private BigDecimal processedQuantity;

    private BigDecimal workerRate;

    private ProductionSubmissionStatus status;

    private String submissionNotes;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;
}