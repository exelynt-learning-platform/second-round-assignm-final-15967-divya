package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.example.demo.DTO.LoginResponse;
import com.example.demo.Entity.User;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/register")
	public User register(@RequestBody User user) {
		return userService.register(user);
	}

	@PostMapping("/login")
	    public  ResponseEntity<LoginResponse> login(@RequestBody User user) {

	        User dbUser = userService.login(user.getEmail(), user.getPassword());
	        System.out.println("dbUser--"+dbUser.getRole());

	        String token = jwtUtil.generateToken(dbUser.getEmail(),dbUser.getRole());
	        return ResponseEntity.ok(new LoginResponse(token, dbUser.getRole()));
	    }
}