package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.LoginRequest;
import com.example.ecommerce.dto.request.RefreshRequest;
import com.example.ecommerce.dto.request.SetPasswordRequest;
import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.dto.response.LoginResponse;
import com.example.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/set-password")
    public ResponseEntity<ApiResponse<String>> setPassword(@Valid @RequestBody SetPasswordRequest req) {

        authService.setPassword(req.getEmail(), req.getPassword());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Password set successfully", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {

        LoginResponse res = authService.login(req.getEmail(), req.getPassword());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Login successful", res));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh( @Valid @RequestBody RefreshRequest req) {

        Map<String, String> res = authService.refresh(req.getEmail(), req.getRefreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Token refreshed", res));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(Authentication auth) {

        authService.logout(auth);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Logged out successfully", null));
    }
}