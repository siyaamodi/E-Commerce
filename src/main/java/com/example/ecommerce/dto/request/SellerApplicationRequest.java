package com.example.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerApplicationRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    private String taxId; // optional
}