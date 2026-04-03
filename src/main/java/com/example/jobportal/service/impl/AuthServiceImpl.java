package com.example.jobportal.service.impl;

import com.example.jobportal.dto.AuthRequest;
import com.example.jobportal.dto.AuthResponse;
import com.example.jobportal.dto.RefreshTokenRequest;
import com.example.jobportal.dto.RegisterRequest;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.ResourceAlreadyExistsException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.security.JwtService;
import com.example.jobportal.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);

        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), java.util.List.of()
        );
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), java.util.List.of()
        );
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String userEmail = jwtService.extractUsername(request.getToken());
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                java.util.List.of()
        );

        if (!jwtService.isTokenValid(request.getToken(), userDetails)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }
}
