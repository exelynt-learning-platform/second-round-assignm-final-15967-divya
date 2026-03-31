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
	
	@Value("${jwt.secret.min-entropy-threshold}")
	private double entropyThreshold;

	@Value("${jwt.secret.key-size}")
	private int keySize;

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

		// 1. Null / empty check
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("JWT secret is missing");
		}

		// 2. Base64 decode validation
		byte[] decoded;
		try {
			decoded = Base64.getDecoder().decode(secret);
		} catch (Exception e) {
			throw new IllegalStateException("JWT secret must be Base64 encoded");
		}




		// Min length
		if (decoded.length < minBytes) {
		    throw new IllegalStateException(
		        String.format(
		            "JWT secret must be at least %d bits (%d bytes)",
		            minBytes * 8, minBytes
		        )
		    );
		}

		// Max length
		if (decoded.length > maxBytes) {
		    throw new IllegalStateException(
		        String.format(
		            "JWT secret must not exceed %d bits (%d bytes)",
		            maxBytes * 8, maxBytes
		        )
		    );
		}

		// ✅ 5. Entropy check (basic randomness validation)
		if (isLowEntropy(decoded)) {
		    throw new IllegalStateException(
		        String.format(
		            "JWT secret has low entropy (uniqueness ratio below %.2f)",
		            entropyThreshold
		        )
		    );
		}
	}

	private boolean isLowEntropy(byte[] data) {
	    long uniqueBytes = java.util.stream.IntStream.range(0, data.length)
	            .map(i -> data[i])
	            .distinct()
	            .count();

	    double uniquenessRatio = (double) uniqueBytes / data.length;

	    return uniquenessRatio < entropyThreshold;
	}

	// ✅ Generate Token
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