package com.hisabkitab.backend.payment.interfaces.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreditAllocationResponse {

    private Long id;

    private Long creditId;

    private Long billId;

    private String billNumber;

    private BigDecimal amount;

    private LocalDateTime allocatedAt;

    private String remarks;
}