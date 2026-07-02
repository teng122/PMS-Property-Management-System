package com.smarthotel.identity_service.service;

import com.smarthotel.identity_service.dto.*;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    TokenValidationResponse validateToken(String token);
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);
}
