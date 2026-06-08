package com.example.ecommerce.controller;

import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.dto.request.EmailRequestDto;
import com.example.ecommerce.dto.response.OtpVerifyDto;
import com.example.ecommerce.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> send( @Valid @RequestBody EmailRequestDto dto) {

        otpService.sendOtp(dto.getEmail());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("OTP sent", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verify( @Valid @RequestBody OtpVerifyDto dto) {

        otpService.verifyOtp(dto.getEmail(), dto.getOtp());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("OTP verified", null));
    }
}