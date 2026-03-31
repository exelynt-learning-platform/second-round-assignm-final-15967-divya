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

	@Value("${jwt.secret.min-bytes}")
	private int minBytes;

	@Value("${jwt.secret.max-bytes}")
	private int maxBytes;

	@Value("${jwt.secret.key-size}")
	private int keySize;

	private SecretKey key;

	@PostConstruct
	public void init() {

		validateSecret(SECRET);

		this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
	}

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

		if (decoded.length < minBytes) {
			throw new IllegalStateException(
					String.format("JWT secret must be at least %d bits (%d bytes)", minBytes * 8, minBytes));
		}

		if (decoded.length > maxBytes) {
			throw new IllegalStateException(
					String.format("JWT secret must not exceed %d bits (%d bytes)", maxBytes * 8, maxBytes));
		}

	}

	public String generateToken(String email, String role) {
		return Jwts.builder().setSubject(email).claim("role", role).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
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

	// 🔥 OPTIONAL: Use ONCE to generate strong secret
	public static String generateStrongSecret(int keySize) {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
			keyGen.init(keySize);

			SecretKey secretKey = keyGen.generateKey();
			return Base64.getEncoder().encodeToString(secretKey.getEncoded());

		} catch (Exception e) {
			throw new RuntimeException("Error generating JWT secret", e);
		}
	}
}