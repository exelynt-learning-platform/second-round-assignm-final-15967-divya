package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // ✅ Register
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    // ✅ Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody User user) {

        User dbUser = userService.login(user.getEmail(), user.getPassword());

        String token = jwtUtil.generateToken(dbUser.getEmail(), dbUser.getRole());

        return ResponseEntity.ok(new LoginResponse(token, dbUser.getRole()));
    }
}