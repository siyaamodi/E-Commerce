package com.example.ecommerce.dto.response;

import com.example.ecommerce.enums.Category;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    @Enumerated(EnumType.STRING)
    private Category category;
    private String imageUrl;
    private String brand;
    private String productStatus;   // ACTIVE, OUT_OF_STOCK, DISCONTINUED
    private String sellerName;      // or seller email
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
