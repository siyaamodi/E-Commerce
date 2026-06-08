package com.example.ecommerce.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerifyDto {
    private String email;
    private String otp;
}