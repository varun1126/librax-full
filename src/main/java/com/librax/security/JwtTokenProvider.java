package com.librax.security;

import com.librax.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtTokenProvider {

    private static final String DEFAULT_DEV_SECRET = "LibraX-Library-Management-Secure-HMAC-SHA256-Key-2026-SuperSecret!";
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L; // 24 hours

    private final SecretKey key;

    public JwtTokenProvider() {
        this(null);
    }

    public JwtTokenProvider(@Value("${jwt.secret:}") String propertySecret) {
        String envSecret = System.getenv("LIBRAX_JWT_SECRET");
        String effectiveSecret = (envSecret != null && !envSecret.isBlank())
                ? envSecret
                : (propertySecret != null && !propertySecret.isBlank() ? propertySecret : DEFAULT_DEV_SECRET);

        // Ensure key has at least 256 bits (32 bytes)
        byte[] keyBytes = effectiveSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole().name());
        claims.put("fullName", user.getFullName());
        claims.put("email", user.getEmail());
        claims.put("permissions", new ArrayList<>(user.getPermissions()));

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
