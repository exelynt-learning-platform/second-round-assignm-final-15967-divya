package com.example.demo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.Entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	// ✅ SUCCESS CASE
	@Test
	void shouldRegisterUserSuccessfully() {

		User user = new User();
		user.setEmail("test@gmail.com");
		user.setPassword("1234");

		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

		when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

		when(userRepository.save(any(User.class))).thenReturn(user);

		User result = userService.register(user);

		assertNotNull(result);
		assertEquals("test@gmail.com", result.getEmail());
	}

	@Test
	void shouldThrowExceptionWhenUserAlreadyExists() {

		User user = new User();
		user.setEmail("test@gmail.com");

		when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

		assertThrows(RuntimeException.class, () -> {
			userService.register(user);
		});
	}
}