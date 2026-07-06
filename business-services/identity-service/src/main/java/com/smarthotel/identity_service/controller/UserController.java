package com.smarthotel.identity_service.controller;

import com.smarthotel.identity_service.dto.RegisterRequest;
import com.smarthotel.identity_service.dto.UserResponse;
import com.smarthotel.identity_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller dành cho quản trị người dùng.
 * Các API ở đây yêu cầu phân quyền ADMIN hệ thống.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private AuthService authService;

    /**
     * Admin tạo tài khoản người dùng với vai trò tùy chọn (ADMIN, RECEPTIONIST, STAFF, CUSTOMER).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerByAdmin(request));
    }
}
