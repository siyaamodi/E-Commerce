package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.AddToCartRequest;
import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.dto.response.CartItemResponse;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> add(
            @Valid @RequestBody AddToCartRequest req,
            Authentication auth) {

        cartService.addToCart(auth.getName(), req);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Added to cart", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> get(Authentication auth) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Cart fetched",
                        cartService.getCart(auth.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> remove(
             @PathVariable Long id,
            Authentication auth) {

        cartService.removeItem(id, auth.getName());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Item removed", null));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clear(Authentication auth) {

        cartService.clearCart(auth.getName());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Cart cleared", null));
    }
}