package com.example.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
}
