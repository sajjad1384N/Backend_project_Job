package com.example.jobportal.controller;

import com.example.jobportal.dto.*;
import com.example.jobportal.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponse.ok("User registered successfully", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        return ApiResponse.ok("Login successful", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.ok("Token refreshed successfully", authService.refresh(request));
    }
}
