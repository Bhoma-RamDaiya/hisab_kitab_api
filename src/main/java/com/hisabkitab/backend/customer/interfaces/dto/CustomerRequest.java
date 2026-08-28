package com.hisabkitab.backend.customer.interfaces.dto;

import com.hisabkitab.backend.customer.domain.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

    @NotNull(message = "Customer type is required.")
    private CustomerType customerType;

    @NotBlank(message = "Customer name is required.")
    private String customerName;

    /**
     * Required only when customer is an existing Hisab Kitab user.
     */
    private Long linkedUserId;

    /**
     * Required only when customer is another Hisab Kitab organization.
     */
    private Long linkedOrganizationId;

    private String phoneNumber;

    @Email(message = "Invalid email.")
    private String email;

    private String address;
}