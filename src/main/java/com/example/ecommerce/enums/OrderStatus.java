package com.example.ecommerce.enums;

public enum OrderStatus {
    CREATED,
    AWAITING_PAYMENT,   // new status
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    PAID
}
