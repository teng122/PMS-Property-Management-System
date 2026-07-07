package com.smarthotel.identity_service.controller;

import com.smarthotel.identity_service.dto.RegisterRequest;
import com.smarthotel.identity_service.dto.UserResponse;
import com.smarthotel.identity_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller dành cho quản trị người dùng.
 * Các API ở đây yêu cầu phân quyền ADMIN hệ thống.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    @Autowired
    private AuthService authService;

    /**
     * Admin tạo tài khoản người dùng với vai trò tùy chọn (ADMIN, RECEPTIONIST, STAFF, CUSTOMER).
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerByAdmin(request));
    }

    /**
     * Admin lấy danh sách toàn bộ tài khoản người dùng.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    /**
     * Admin thay đổi quyền của một tài khoản (truyền role qua query param).
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable("id") UUID id,
            @RequestParam("role") String role) {
        return ResponseEntity.ok(authService.updateUserRole(id, role));
    }

    /**
     * Admin khóa/mở khóa một tài khoản người dùng.
     */
    @PutMapping("/{id}/block")
    public ResponseEntity<UserResponse> toggleUserBlockStatus(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(authService.toggleUserBlockStatus(id));
    }
}
