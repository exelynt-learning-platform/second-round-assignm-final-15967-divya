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
		validateSecret(SECRET);
		this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
	}

	// ✅ Strong validation (improved)
	private void validateSecret(String secret) {

		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("JWT secret is missing");
		}

		byte[] decoded;
		try {
			decoded = Base64.getDecoder().decode(secret);
		} catch (Exception e) {
			throw new IllegalStateException("JWT secret must be Base64 encoded");
		}

		if (decoded.length < 32) {
			throw new IllegalStateException("JWT secret must be at least 256 bits");
		}

		// ❗ Basic entropy check (avoid weak patterns)
		if (secret.matches("(.)\\1{10,}")) {
			throw new IllegalStateException("JWT secret is too weak (repeating pattern)");
		}
	}

	// ✅ Generate Token
	public String generateToken(String email, String role) {
		return Jwts.builder().setSubject(email).claim("role", role) // ROLE_USER
				.setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
				.signWith(key, SignatureAlgorithm.HS256).compact();
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
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	// 🔥 ✅ Use this ONCE to generate strong secret (production)
	public static String generateStrongSecret() {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
			keyGen.init(256); // 256-bit key
			SecretKey secretKey = keyGen.generateKey();
			return Base64.getEncoder().encodeToString(secretKey.getEncoded());
		} catch (Exception e) {
			throw new RuntimeException("Error generating JWT secret", e);
		}
	}

}