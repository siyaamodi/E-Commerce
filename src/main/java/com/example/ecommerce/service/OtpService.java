package com.example.ecommerce.service;

import com.example.ecommerce.entity.Otp;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.enums.UserStatus;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.TooManyRequestsException;
import com.example.ecommerce.repository.OtpRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
@Service
@RequiredArgsConstructor
public class OtpService {

    private final UserRepository userRepo;
    private final OtpRepository otpRepo;
    private final EmailService emailService;

    public void sendOtp(String email) {

        User user = userRepo.findByEmail(email).orElse(null);

        // ✅ BLOCK CHECK – only for existing users
        if (user != null && user.getBlockedUntil() != null && LocalDateTime.now().isBefore(user.getBlockedUntil())) {
            throw new BadRequestException("User is blocked until " + user.getBlockedUntil());
        }

        // Existing registered active user cannot request OTP again
        if (user != null && user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("User already registered");
        }

        // Create new user if not exists
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setStatus(UserStatus.PENDING);
            user.setRole(Role.USER);
            userRepo.save(user);
        }

        // Generate and save OTP (same as before)
        String otpValue = String.format("%06d", new Random().nextInt(1000000));
        Otp otp = otpRepo.findByEmail(email).orElse(new Otp());
        otp.setEmail(email);
        otp.setOtp(otpValue);
        otp.setAttempts(0);
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otp.setLastSentTime(LocalDateTime.now());
        otpRepo.save(otp);
        emailService.sendOtp(email, otpValue);
    }
    public void verifyOtp(String email, String inputOtp) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getBlockedUntil() != null && LocalDateTime.now().isBefore(user.getBlockedUntil())) {
            throw new BadRequestException("User is blocked until " + user.getBlockedUntil());
        }

        Otp otp = otpRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found"));

        if (LocalDateTime.now().isAfter(otp.getExpiryTime())) {
            throw new BadRequestException("OTP expired");
        }

        if (!otp.getOtp().equals(inputOtp)) {

            otp.setAttempts(otp.getAttempts() + 1);

            if (otp.getAttempts() >= 6) {
                user.setStatus(UserStatus.BLOCKED);
                user.setBlockedUntil(LocalDateTime.now().plusHours(24));
                userRepo.save(user);
                throw new TooManyRequestsException("Too many attempts");
            }

            otpRepo.save(otp);
            throw new BadRequestException("Invalid OTP");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.VERIFIED);
        userRepo.save(user);

        otpRepo.delete(otp);
    }
}