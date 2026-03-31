package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public User register(User user) {

		Optional<User> existing = userRepository.findByEmail(user.getEmail());

		if (existing.isPresent()) {
			throw new RuntimeException("Email already exists");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));
	    user.setRole(Role.ROLE_USER.name());

		return userRepository.save(user);
	}

	public User login(String email, String password) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new BadCredentialsException("Invalid password");
		}

		return user;
	}
}