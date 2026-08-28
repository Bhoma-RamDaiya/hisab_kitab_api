package com.hisabkitab.backend.buyer.interfaces;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerRequest {

    @NotBlank(message = "Buyer name is required.")
    private String name;

    private Long userId;

    private String phoneNumber;

    private String relation;
}