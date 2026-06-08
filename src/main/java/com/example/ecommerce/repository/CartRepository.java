package com.example.ecommerce.repository;

import com.example.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserEmail(String email);

    Optional<CartItem> findByUserEmailAndProduct_Id(String email, Long productId);
    @Modifying
    @Transactional
    void deleteByUserEmail(String email);
}