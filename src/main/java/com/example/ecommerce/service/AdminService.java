package com.example.ecommerce.service;

import com.example.ecommerce.dto.response.SellerApplicationResponse;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.enums.SellerStatus;
import com.example.ecommerce.enums.UserStatus;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional
    public void blockUserPermanently(String email) {
        User user = findUser(email);
        user.setStatus(UserStatus.BLOCKED);
        user.setBlockedUntil(null);
        userRepository.save(user);
    }

    @Transactional
    public void blockUserTemporarily(String email, int hours) {
        User user = findUser(email);
        user.setBlockedUntil(LocalDateTime.now().plusHours(hours));
        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(String email) {
        User user = findUser(email);
        user.setStatus(UserStatus.ACTIVE);
        user.setBlockedUntil(null);
        userRepository.save(user);
    }

    public List<User> getAllSellers() {
        return userRepository.findByRole(Role.SELLER);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public void setUserRole(String email, Role role) {
        User user = findUser(email);
        user.setRole(role);
        userRepository.save(user);
    }

    public List<SellerApplicationResponse> getPendingSellerApplications() {
        return userRepository.findBySellerStatus(SellerStatus.PENDING)
                .stream()
                .map(user -> new SellerApplicationResponse(
                        user.getEmail(),
                        user.getName(),
                        user.getPhoneNumber(),
                        user.getCompanyName(),
                        user.getBusinessAddress(),
                        user.getTaxId(),
                        user.getSellerStatus().name()
                ))
                .toList();
    }

    public void approveSeller(String email) {
        User user = findUser(email);
        if (user.getSellerStatus() != SellerStatus.PENDING) {
            throw new BadRequestException("No pending application");
        }
        user.setSellerStatus(SellerStatus.APPROVED);
        user.setRole(Role.SELLER);
        userRepository.save(user);
    }

    public void rejectSeller(String email) {
        User user = findUser(email);
        if (user.getSellerStatus() != SellerStatus.PENDING) {
            throw new BadRequestException("No pending application");
        }
        user.setSellerStatus(SellerStatus.REJECTED);
        userRepository.save(user);
    }
}