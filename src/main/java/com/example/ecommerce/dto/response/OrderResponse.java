package com.example.ecommerce.dto.response;

import com.example.ecommerce.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private double totalAmount;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private String deliveryFullName;
    private String deliveryPhone;
    private String deliveryAddress;
}