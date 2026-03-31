package com.example.demo.security;

import java.util.Base64;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {

        // ✅ CHANGE: Strong validation added (length + Base64 + entropy)
        validateSecret(SECRET);

        // ✅ CHANGE: Key derived securely from Base64 decoded secret
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
    }

    // 🔥 ✅ CHANGE: Improved validation (CRYPTO-READY)
    private void validateSecret(String secret) {

        // Check null / empty
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is missing");
        }

        // Check Base64 format
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret);
        } catch (Exception e) {
            throw new IllegalStateException("JWT secret must be Base64 encoded");
        }

        // ✅ CHANGE: Minimum 256-bit (32 bytes) requirement
        if (decoded.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 256 bits (32 bytes)");
        }

        // ✅ CHANGE: Entropy check (avoid weak repeating patterns)
        if (secret.matches("(.)\\1{10,}")) {
            throw new IllegalStateException("JWT secret is too weak (repeating pattern detected)");
        }
    }

    // ✅ Generate Token
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 🔥 OPTIONAL: Use ONCE to generate strong secret
    public static String generateStrongSecret() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            keyGen.init(256); // 256-bit

            SecretKey secretKey = keyGen.generateKey();

            return Base64.getEncoder().encodeToString(secretKey.getEncoded());

        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT secret", e);
        }
    }
}