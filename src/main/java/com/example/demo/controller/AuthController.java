package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.LoginResponse;
import com.example.demo.DTO.RegisterRequest;
import com.example.demo.Entity.User;
import com.example.demo.constants.AppConstants;
import com.example.demo.enums.Role;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/register")
	public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
		if (!List.of("ROLE_USER", "ROLE_ADMIN").contains(request.getRole())) {
			throw new IllegalArgumentException("Invalid role");
		}

		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(request.getPassword());
		 user.setRole(request.getRole() != null ? request.getRole() : Role.ROLE_USER.name());
		userService.register(user);

		return ResponseEntity.ok(AppConstants.USER_REGISTERED_SUCCESS);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

		User dbUser = userService.login(request.getEmail(), request.getPassword());

		String token = jwtUtil.generateToken(dbUser.getEmail(), dbUser.getRole());

		return ResponseEntity.ok(new LoginResponse(token, dbUser.getRole()));
	}
}