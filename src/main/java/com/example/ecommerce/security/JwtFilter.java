package com.example.ecommerce.security;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.UserStatus;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            User user = userRepo.findByEmail(email).orElse(null);
// 🔥 BLOCK CHECKS
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }
            if (user.getStatus() == UserStatus.BLOCKED) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account blocked");
                return;
            }
            if (user.getBlockedUntil() != null && LocalDateTime.now().isBefore(user.getBlockedUntil())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account temporarily blocked");
                return;
            }
            if (user != null && jwtService.isValid(token, user)) {


                CustomUserDetails userDetails = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                     // ← principal is CustomUserDetails
                                null,
                                userDetails.getAuthorities()     // or List.of(...)
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}