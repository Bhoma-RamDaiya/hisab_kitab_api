package com.hisabkitab.backend.billing.interfaces.dto;

import com.hisabkitab.backend.billing.domain.BillStatus;
import com.hisabkitab.backend.billing.interfaces.dto.BillItemResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponse {

    private Long id;

    private Long organizationId;

    private Long customerId;

    private String billNumber;

    private LocalDateTime billDate;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private BigDecimal pendingAmount;

    private BillStatus status;

    private String notes;

    private List<BillItemResponse> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}