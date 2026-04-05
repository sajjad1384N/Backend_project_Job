package com.example.jobportal.service.impl;

import com.example.jobportal.dto.AuthRequest;
import com.example.jobportal.dto.AuthResponse;
import com.example.jobportal.dto.ForgotPasswordRequest;
import com.example.jobportal.dto.RefreshTokenRequest;
import com.example.jobportal.dto.RegisterRequest;
import com.example.jobportal.dto.ResetPasswordRequest;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.ResourceAlreadyExistsException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.security.JwtService;
import com.example.jobportal.service.ApplicationStatusMailer;
import com.example.jobportal.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ApplicationStatusMailer applicationStatusMailer;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

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

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        if (!mailEnabled) {
            log.warn("Password reset requested but app.mail.enabled=false; email not sent");
            return;
        }
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String raw = UUID.randomUUID().toString().replace("-", "")
                    + UUID.randomUUID().toString().replace("-", "");
            user.setPasswordResetTokenHash(passwordEncoder.encode(raw));
            user.setPasswordResetExpiresAt(Instant.now().plusSeconds(3600));
            userRepository.save(user);
            String base = frontendBaseUrl.replaceAll("/$", "");
            String link = base + "/reset-password?token=" + URLEncoder.encode(raw, StandardCharsets.UTF_8)
                    + "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
            try {
                applicationStatusMailer.sendPasswordResetEmail(user.getEmail(), link);
            } catch (Exception e) {
                log.warn("Could not send password reset email: {}", e.getMessage());
            }
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));
        if (user.getPasswordResetTokenHash() == null
                || user.getPasswordResetExpiresAt() == null
                || user.getPasswordResetExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired reset link");
        }
        if (!passwordEncoder.matches(request.getToken(), user.getPasswordResetTokenHash())) {
            throw new IllegalArgumentException("Invalid or expired reset link");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(null);
        userRepository.save(user);
    }
}
