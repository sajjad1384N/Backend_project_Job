package com.example.jobportal.service.interfaces;

import com.example.jobportal.dto.AuthRequest;
import com.example.jobportal.dto.AuthResponse;
import com.example.jobportal.dto.ForgotPasswordRequest;
import com.example.jobportal.dto.RefreshTokenRequest;
import com.example.jobportal.dto.RegisterRequest;
import com.example.jobportal.dto.ResetPasswordRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    AuthResponse refresh(RefreshTokenRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
