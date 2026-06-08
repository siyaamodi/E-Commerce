package com.example.ecommerce.entity;

import com.example.ecommerce.enums.Role;
import com.example.ecommerce.enums.SellerStatus;
import com.example.ecommerce.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @Enumerated(EnumType.STRING)
    private Role role;
    private int failedLoginAttempts;
    private LocalDateTime accountLockedUntil;
    private String phoneNumber;
    private LocalDateTime tokenInvalidBefore;
    private LocalDateTime blockedUntil;
    @Enumerated(EnumType.STRING)
    private SellerStatus sellerStatus = SellerStatus.NOT_APPLIED;
    private String companyName;
    private String businessAddress;
    private String taxId;  // GST/PAN/VAT
    private boolean sellerApproved = false;
    private boolean sellerRequestPending = false;
    private boolean emailVerified = false;
    private boolean resetVerified = false;
}