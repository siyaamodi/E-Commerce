package com.example.ecommerce.service;

import com.example.ecommerce.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKeyBase64;

    @Value("${jwt.expiration-ms:86400000}") // default 24h
    private long expirationMs;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyBase64));
    }

    // ✅ Accept email (String) – simpler, more flexible
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ✅ Now checks tokenInvalidBefore
    public boolean isValid(String token, User user) {
        Claims claims = extractAllClaims(token);
        Date issuedAt = claims.getIssuedAt();

        // If user has a tokenInvalidBefore timestamp, reject tokens issued before that
        if (user.getTokenInvalidBefore() != null) {
            Date invalidBeforeDate = Date.from(user.getTokenInvalidBefore()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant());
            if (issuedAt.before(invalidBeforeDate)) {
                return false;
            }
        }

        return claims.getSubject().equals(user.getEmail())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}