package com.smarthotel.identity_service.controller;

import com.smarthotel.identity_service.dto.*;
import com.smarthotel.identity_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("id") java.util.UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader) {
        
        // Kiểm tra quyền sở hữu đối với vai trò CUSTOMER (IDOR / BOLA Prevention)
        if (userRoleHeader != null && userRoleHeader.contains("ROLE_CUSTOMER")) {
            if (userIdHeader == null || !id.toString().equalsIgnoreCase(userIdHeader)) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập thông tin tài khoản này!"
                );
            }
        }
        return ResponseEntity.ok(authService.getUserById(id));
    }
}
