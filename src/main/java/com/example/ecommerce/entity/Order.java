package com.example.ecommerce.entity;

import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.enums.PaymentMethod;
import com.example.ecommerce.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private String userEmail;

    private double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String deliveryPartnerEmail;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime paymentExpiry;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;  // "COD" or "ONLINE"

    private String deliveryFullName;
    private String deliveryPhone;
    private String deliveryAddressLine;  // street, city, state, pincode combined
}