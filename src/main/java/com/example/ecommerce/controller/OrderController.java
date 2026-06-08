package com.example.ecommerce.controller;

import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.dto.response.PageResponse;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.enums.PaymentMethod;
import com.example.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 🔥 GET ORDERS (ADVANCED)
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
            Authentication auth,
            @RequestParam(required = false) OrderStatus status,

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam(required = false) LocalDateTime fromDate,

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam(required = false) LocalDateTime toDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        "Orders fetched",
                        orderService.getMyOrders(
                                auth.getName(),
                                status,
                                fromDate,
                                toDate,
                                page,
                                size,
                                sortBy,
                                direction
                        )
                )
        );
    }

    // 🔥 PLACE ORDER
    @PostMapping("/place")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderResponse>> place(
            @RequestParam(defaultValue = "ONLINE") PaymentMethod paymentMethod,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "Order placed",
                        orderService.placeOrder(auth.getName(), paymentMethod)
                ));
    }

    // 🔥 CANCEL
    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> cancel(
            @PathVariable Long id,
            Authentication auth) {

        orderService.cancelOrder(id, auth.getName());

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>("Order cancelled", null)
        );
    }

    // 🔥 UPDATE STATUS (ADMIN)
    @PostMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> update(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        orderService.updateStatus(id, status);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>("Status updated", null)
        );
    }

    // 🔥 PAY ORDER
    @PostMapping("/pay/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> pay(
            @PathVariable Long id,
            Authentication auth) {

        orderService.payOrder(id, auth.getName());

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>("Payment successful", null)
        );
    }
    @PostMapping("/retry-payment/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> retry(
            @PathVariable Long id,
            Authentication auth) {

        orderService.retryPayment(id, auth.getName());

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>("Payment retried successfully", null)
        );
    }
    @PostMapping("/refund/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> refund(
            @PathVariable Long id,
            Authentication auth) {

        orderService.refundOrder(id, auth.getName());

        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>("Refund successful", null)
        );
    }
    @GetMapping("/{orderId}/delivery-info")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDeliveryInfo(
            @PathVariable Long orderId,
            Authentication auth) {
        return ResponseEntity.ok(new ApiResponse<>(
                "Delivery info fetched",
                orderService.getDeliveryInfoForUser(orderId, auth.getName())
        ));
    }
}