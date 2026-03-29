package com.example.demo.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

	private final String SECRET = "mysecretkeymysecretkeymysecretkey12345";

	private final SecretKey SECRET_KEY =
	        Keys.hmacShaKeyFor(SECRET.getBytes());

	
	
	public String generateToken(String email,String role) {
		System.out.println("Role while login: " + role);
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) 
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Extract email
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }
    // Validate token
    public boolean validateToken(String token, String email) {
        return extractEmail(token).equals(email);
    }
}