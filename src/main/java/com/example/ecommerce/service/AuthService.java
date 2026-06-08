package com.example.ecommerce.service;

import com.example.ecommerce.dto.response.LoginResponse;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface AuthService {

    void setPassword(String email, String password);

    LoginResponse login(String email, String password);

    Map<String, String> refresh(String email, String token);

    void logout(Authentication auth);
}