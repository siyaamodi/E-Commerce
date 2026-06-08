package com.example.ecommerce.entity;

import com.example.ecommerce.enums.Category;
import com.example.ecommerce.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Double price;
    private Integer stock;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Version
    private Long version;

    // 🔥 NEW FIELDS
    private String imageUrl;          // product image URL
    private String brand;             // product brand

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ACTIVE;  // default ACTIVE

    // ✅ Seller relation (instead of just sellerEmail string)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    // ✅ Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    public void updateStatusBasedOnStock() {

        // Do not override DISCONTINUED status
        if (this.status == ProductStatus.DISCONTINUED) {
            return;
        }
        if (this.stock == null || this.stock <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else {
            this.status = ProductStatus.ACTIVE;
        }
    }
}