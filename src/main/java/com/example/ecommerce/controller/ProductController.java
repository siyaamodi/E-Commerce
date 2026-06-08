package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.ProductRequest;
import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.dto.response.PageResponse;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.enums.ProductStatus;
import com.example.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 🔥 CREATE
    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> add(
            @Valid @RequestBody ProductRequest req,
            Authentication auth) {

        ProductResponse product =
                productService.addProduct(req, auth.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Product created", product));
    }

    // 🔥 MERGED GET
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "Products fetched",
                        productService.getProducts(
                                name, maxPrice, page, size, sortBy, direction
                        )
                ));
    }

    // 🔥 UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest req,
            Authentication auth) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "Product updated",
                        productService.updateProduct(id, req, auth.getName())
                ));
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status,
            Authentication auth) {
        productService.updateProductStatus(id, status, auth.getName());
        return ResponseEntity.ok(new ApiResponse<>("Status updated", null));
    }
    // 🔥 DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long id,
            Authentication auth) {

        productService.deleteProduct(id, auth.getName());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("Product deleted", null));
    }
}