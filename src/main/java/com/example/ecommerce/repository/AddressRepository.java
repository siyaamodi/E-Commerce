package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserEmailOrderByIsDefaultDesc(String userEmail);

    Optional<Address> findByIdAndUserEmail(Long id, String userEmail);

    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userEmail = :userEmail")
    void clearDefaultFlag(String userEmail);
}