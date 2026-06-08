package com.example.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SellerApplicationResponse {
    private String email;
    private String name;
    private String phoneNumber;
    private String companyName;
    private String businessAddress;
    private String taxId;
    private String sellerStatus;  // PENDING, APPROVED, REJECTED
}