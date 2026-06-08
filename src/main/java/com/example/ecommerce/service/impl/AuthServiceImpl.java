package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.response.LoginResponse;
import com.example.ecommerce.entity.RefreshToken;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.enums.UserStatus;
import com.example.ecommerce.exception.*;
import com.example.ecommerce.repository.RefreshTokenRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.AuthService;
import com.example.ecommerce.service.JwtService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshRepo;

    // Caffeine cache: email -> attempt count, expires 5 minutes after last write
    private final Cache<String, Integer> requestCount = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Override
    public void setPassword(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Email not verified");
        }
        if (user.getPassword() != null) {
            throw new BadRequestException("Password already set");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(UserStatus.ACTIVE);
        // 👇 Set default role if not already set
        if (user.getRole() == null) {
            user.setRole(Role.USER);   // default to USER
        }
        userRepo.save(user);
    }

    @Override
    public LoginResponse login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
// 🔥 BLOCK CHECKS
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BadRequestException("Your account has been blocked. Contact admin.");
        }
        if (user.getBlockedUntil() != null && LocalDateTime.now().isBefore(user.getBlockedUntil())) {
            throw new BadRequestException("Account temporarily blocked until " + user.getBlockedUntil());
        }
        // Get current attempt count (0 if not present)
        Integer attempts = requestCount.getIfPresent(email);
        if (attempts == null) attempts = 0;

        // Block if too many attempts (before any other checks to prevent abuse)
        if (attempts >= 10) {
            throw new TooManyRequestsException("Too many login attempts. Try again later.");
        }

        // Validate user state (these do NOT increment attempts)
        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Verify email first");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Complete registration first");
        }
        if (user.getAccountLockedUntil() != null &&
                LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
            throw new BadRequestException("Account locked");
        }

        // Password check
        if (user.getPassword() == null || !passwordEncoder.matches(password, user.getPassword())) {
            // Increment attempt counter (only on failure)
            requestCount.put(email, attempts + 1);

            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            userRepo.save(user);
            throw new BadRequestException("Invalid credentials");
        }

        // ✅ Login success – reset all failure markers
        requestCount.invalidate(email);          // clear rate-limit cache
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepo.save(user);

        // Invalidate old refresh tokens
        refreshRepo.deleteByEmail(email);

        // Generate new tokens
        String accessToken = jwtService.generateToken(email);
        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedToken = passwordEncoder.encode(rawRefreshToken);
        refreshRepo.save(new RefreshToken(null, hashedToken, email, LocalDateTime.now().plusDays(7)));

        return new LoginResponse(accessToken, rawRefreshToken, 3600);
    }

    @Override
    public Map<String, String> refresh(String email, String token) {
        RefreshToken stored = refreshRepo.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (!passwordEncoder.matches(token, stored.getToken())) {
            throw new BadRequestException("Invalid refresh token");
        }
        if (stored.getExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expired");
        }

        String newAccess = jwtService.generateToken(email);
        return Map.of("accessToken", newAccess);
    }

    @Override
    public void logout(Authentication auth) {
        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setTokenInvalidBefore(LocalDateTime.now());
        userRepo.save(user);
        refreshRepo.deleteByEmail(email);
    }
}