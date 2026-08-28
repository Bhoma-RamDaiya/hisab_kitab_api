package com.hisabkitab.backend.order.interfaces.dto;

import com.hisabkitab.backend.order.interfaces.dto.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    /**
     * Person who actually handed over/gave the order.
     * Optional because sometimes the customer itself gives the order.
     */
    private Long buyerId;

    private LocalDateTime orderDate;

    @NotEmpty(message = "Order must contain at least one item.")
    @Valid
    private List<OrderItemRequest> items;

    private String notes;
}