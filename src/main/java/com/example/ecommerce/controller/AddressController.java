package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.AddressRequest;
import com.example.ecommerce.dto.response.AddressResponse;
import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @Valid @RequestBody AddressRequest req,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Address added", addressService.addAddress(auth.getName(), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest req,
            Authentication auth) {
        return ResponseEntity.ok(new ApiResponse<>("Address updated",
                addressService.updateAddress(id, auth.getName(), req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @PathVariable Long id,
            Authentication auth) {
        addressService.deleteAddress(id, auth.getName());
        return ResponseEntity.ok(new ApiResponse<>("Address deleted", null));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<String>> setDefault(
            @PathVariable Long id,
            Authentication auth) {
        addressService.setDefaultAddress(id, auth.getName());
        return ResponseEntity.ok(new ApiResponse<>("Default address set", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses(Authentication auth) {
        return ResponseEntity.ok(new ApiResponse<>("Addresses fetched", addressService.getUserAddresses(auth.getName())));
    }

    @GetMapping("/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefault(Authentication auth) {
        return ResponseEntity.ok(new ApiResponse<>("Default address fetched", addressService.getDefaultAddress(auth.getName())));
    }
}