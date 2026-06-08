package com.example.ecommerce.service;

import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.dto.response.PageResponse;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.enums.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {

    // 🔥 PLACE ORDER
    OrderResponse placeOrder(String email, PaymentMethod paymentMethod);

    // 🔥 ADVANCED GET (pagination + filter + sort)
    PageResponse<OrderResponse> getMyOrders(
            String email,
            OrderStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size,
            String sortBy,
            String direction
    );

    // 🔥 CANCEL
    void cancelOrder(Long id, String email);

    // 🔥 UPDATE STATUS (ADMIN)
    void updateStatus(Long id, OrderStatus status);

    // 🔥 PAYMENT
    void payOrder(Long id, String email);
    void retryPayment(Long orderId, String email);
    void refundOrder(Long orderId, String email);
    // Delivery partner methods
    void assignOrderToDeliveryPartner(Long orderId, String partnerEmail);
    void pickupOrder(Long orderId, String partnerEmail);
    void deliverOrder(Long orderId, String partnerEmail);
    Map<String, Object> getDeliveryInfoForUser(Long orderId, String userEmail);
    List<OrderResponse> getAssignedOrders(String partnerEmail);
}