package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OrderRepository extends
        JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {
    @EntityGraph(attributePaths = "items")
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);
    List<Order> findByDeliveryPartnerEmail(String partnerEmail);
}