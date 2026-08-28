package com.hisabkitab.backend.payment.interfaces.dto;

import com.hisabkitab.backend.payment.domain.PaymentMethod;
import com.hisabkitab.backend.payment.interfaces.dto.PaymentAllocationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "Payment amount is required.")
    @DecimalMin(
            value = "0.01",
            message = "Payment amount must be greater than zero."
    )
    private BigDecimal amount;

    private LocalDateTime paymentDate;

    @NotNull(message = "Payment method is required.")
    private PaymentMethod paymentMethod;

    /**
     * Optional.
     * Person who actually made the payment.
     */
    private Long payerId;

    private String referenceNumber;

    private String notes;

    /**
     * Optional.
     *
     * Empty = advance/unallocated payment.
     */
    @Valid
    private List<PaymentAllocationRequest> allocations;
}