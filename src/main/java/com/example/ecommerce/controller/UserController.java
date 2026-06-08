package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.SellerApplicationRequest;
import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SellerService sellerService;

    @Operation(summary = "Get logged-in user profile")
    @GetMapping("/profile")
    public String profile(Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return auth.getName();
    }

    @PostMapping("/apply-seller")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<ApiResponse<String>> applyForSeller(
            @Valid @RequestBody SellerApplicationRequest dto,
            Authentication auth) {
        sellerService.applyForSeller(auth.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Seller application submitted", null));
    }
}