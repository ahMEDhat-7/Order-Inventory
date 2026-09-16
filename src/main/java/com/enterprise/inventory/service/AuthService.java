package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.request.LoginRequest;
import com.enterprise.inventory.dto.request.RegisterRequest;
import com.enterprise.inventory.dto.response.AuthResponse;
import com.enterprise.inventory.model.User;
import com.enterprise.inventory.repository.UserRepository;
import com.enterprise.inventory.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        org.springframework.security.core.userdetails.UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPasswordHash(),
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole()))
                );

        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .build();
    }
}
