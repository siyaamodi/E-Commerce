package com.example.ecommerce.controller;

import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.dto.request.EmailRequestDto;
import com.example.ecommerce.dto.response.OtpVerifyDto;
import com.example.ecommerce.dto.request.ResetPasswordRequest;
import com.example.ecommerce.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService service;

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<String>> forgot(@Valid @RequestBody EmailRequestDto req) {

        service.sendResetOtp(req.getEmail());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("OTP sent", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verify(@Valid @RequestBody OtpVerifyDto req) {

        service.verifyResetOtp(req.getEmail(), req.getOtp());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("OTP verified", null));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<String>> reset(@Valid @RequestBody ResetPasswordRequest req) {

        service.resetPassword(req.getEmail(), req.getNewPassword());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Password reset successful", null));
    }
}