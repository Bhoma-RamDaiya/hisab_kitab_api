package com.hisabkitab.backend.dto.request;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationRegistrationRequestDto {

//    @NotBlank(message = "Organization name is required")
    private String organizationName;

//    @NotBlank(message = "Owner name is required")
    private String ownerName;

//    @NotBlank(message = "PAN number is required")
    private String panNumber;

    private String gstNumber;

//    @NotBlank(message = "Aadhaar number is required")
    private String aadhaarNumber;

//    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

//    @NotBlank(message = "Business address is required")
    private String address;
}
