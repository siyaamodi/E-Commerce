package com.example.ecommerce.service;


import com.example.ecommerce.entity.Otp;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.UserStatus;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.UnauthorizedException;
import com.example.ecommerce.repository.OtpRepository;
import com.example.ecommerce.repository.RefreshTokenRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepo;
    private final OtpRepository otpRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    public void sendResetOtp(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResourceNotFoundException("User not active");
        }

        String otpValue = String.format("%06d", new Random().nextInt(1000000));

        Otp otp = otpRepo.findByEmail(email).orElse(new Otp());

        otp.setEmail(email);
        otp.setOtp(otpValue);
        otp.setAttempts(0);
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepo.save(otp);
        emailService.sendOtp(email, otpValue);
    }

    public void verifyResetOtp(String email, String otpInput) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Otp otp = otpRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found"));

        if (LocalDateTime.now().isAfter(otp.getExpiryTime())) {
            throw new BadRequestException("OTP expired");
        }

        if (!otp.getOtp().equals(otpInput)) {
            throw new BadRequestException("Invalid OTP");
        }

        user.setResetVerified(true);
        userRepo.save(user);

        otpRepo.delete(otp);
    }

    public void resetPassword(String email, String newPassword) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isResetVerified()) {
            throw new UnauthorizedException("Verify OTP first");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenInvalidBefore(LocalDateTime.now());
        user.setResetVerified(false);

        userRepo.save(user);
        refreshTokenRepository.deleteByEmail(email);
    }
}
