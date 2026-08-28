package com.hisabkitab.backend.buyer.interfaces;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerResponse {

    private Long id;

    private Long customerId;

    private Long userId;

    private String name;

    private String phoneNumber;

    private String relation;

    private BuyerStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}