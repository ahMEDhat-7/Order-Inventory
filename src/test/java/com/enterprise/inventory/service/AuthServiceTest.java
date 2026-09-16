package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.request.LoginRequest;
import com.enterprise.inventory.dto.request.RegisterRequest;
import com.enterprise.inventory.dto.response.AuthResponse;
import com.enterprise.inventory.model.User;
import com.enterprise.inventory.repository.UserRepository;
import com.enterprise.inventory.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldSaveUser() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");

        authService.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void login_shouldReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashed_password")
                .role("ROLE_USER")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt_token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt_token", response.getToken());
        assertEquals("Bearer", response.getType());
    }

    @Test
    void login_shouldThrowOnInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("wrong_password");

        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }
}
