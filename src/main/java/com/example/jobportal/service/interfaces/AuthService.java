package com.example.jobportal.service.interfaces;

import com.example.jobportal.dto.AuthRequest;
import com.example.jobportal.dto.AuthResponse;
import com.example.jobportal.dto.RefreshTokenRequest;
import com.example.jobportal.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
}
