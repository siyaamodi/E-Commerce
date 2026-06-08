package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.SellerApplicationRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.enums.SellerStatus;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final UserRepository userRepo;

    @Transactional
    public void applyForSeller(String email, SellerApplicationRequest request) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getSellerStatus() == SellerStatus.PENDING) {
            throw new BadRequestException("Already applied, waiting for approval");
        }
        if (user.getSellerStatus() == SellerStatus.APPROVED) {
            throw new BadRequestException("You are already a seller");
        }
        user.setSellerStatus(SellerStatus.PENDING);
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCompanyName(request.getCompanyName());
        user.setBusinessAddress(request.getBusinessAddress());
        user.setTaxId(request.getTaxId());
        userRepo.save(user);
    }
}