package com.example.ecommerce.controller;

import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DELIVERY_PARTNER')")
public class DeliveryPartnerController {

    private final OrderService orderService;

    @GetMapping("/assigned-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAssignedOrders(Authentication auth) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Assigned orders fetched",
                orderService.getAssignedOrders(auth.getName())
        ));
    }

    @PostMapping("/pickup/{orderId}")
    public ResponseEntity<ApiResponse<String>> pickupOrder(
            @PathVariable Long orderId,
            Authentication auth) {
        orderService.pickupOrder(orderId, auth.getName());
        return ResponseEntity.ok(new ApiResponse<>("Order picked up", null));
    }

    @PostMapping("/deliver/{orderId}")
    public ResponseEntity<ApiResponse<String>> deliverOrder(
            @PathVariable Long orderId,
            Authentication auth) {
        orderService.deliverOrder(orderId, auth.getName());
        return ResponseEntity.ok(new ApiResponse<>("Order delivered", null));
    }
}
