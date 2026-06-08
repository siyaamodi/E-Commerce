package com.example.ecommerce.enums;

public enum SellerStatus {
    NOT_APPLIED,   // never applied
    PENDING,       // application submitted, waiting admin
    APPROVED,      // approved by admin (can sell)
    REJECTED       // rejected by admin
}