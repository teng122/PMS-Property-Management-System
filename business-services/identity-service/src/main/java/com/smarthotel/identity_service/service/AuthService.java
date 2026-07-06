package com.smarthotel.identity_service.service;

import com.smarthotel.identity_service.dto.*;
import com.smarthotel.identity_service.entity.RoleName;
import com.smarthotel.identity_service.entity.User;
import com.smarthotel.identity_service.repository.UserRepository;
import com.smarthotel.identity_service.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên tài khoản đã tồn tại");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại");
        }

        // Đăng ký công khai luôn ép buộc là ROLE_CUSTOMER
        String role = RoleName.ROLE_CUSTOMER.name();

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setFullName(savedUser.getFullName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        return response;
    }

    public UserResponse registerByAdmin(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên tài khoản đã tồn tại");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại");
        }

        String roleInput = request.getRole();
        String finalRole;
        if (roleInput == null || roleInput.trim().isEmpty()) {
            finalRole = RoleName.ROLE_CUSTOMER.name();
        } else {
            String normalized = roleInput.trim().toUpperCase();
            if (!normalized.startsWith("ROLE_")) {
                normalized = "ROLE_" + normalized;
            }
            try {
                RoleName roleEnum = RoleName.valueOf(normalized);
                finalRole = roleEnum.name();
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quyền (role) không hợp lệ. Hợp lệ: ADMIN, RECEPTIONIST, STAFF, CUSTOMER");
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(finalRole)
                .build();

        User savedUser = userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setFullName(savedUser.getFullName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tên tài khoản hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tên tài khoản hoặc mật khẩu không chính xác");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .refreshToken(refreshToken)
                .build();
    }

    public TokenValidationResponse validateToken(String token) {
        boolean valid = jwtService.validateToken(token);
        if (valid) {
            String username = jwtService.getUsernameFromToken(token);
            String role = jwtService.getRoleFromToken(token);
            return new TokenValidationResponse(true, username, role);
        }
        return new TokenValidationResponse(false, null, null);
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String token = request.getRefreshToken();
        if (token == null || token.trim().isEmpty() || !jwtService.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token không hợp lệ hoặc đã hết hạn");
        }

        String username = jwtService.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token không hợp lệ hoặc đã hết hạn"));

        // PHÁT HIỆN TẤN CÔNG TÁI SỬ DỤNG (REUSE DETECTION):
        // Nếu token gửi lên hợp lệ nhưng không khớp với token đang lưu trong DB,
        // chứng tỏ token này là một token cũ đã được xoay vòng trước đó -> Hủy ngay lập tức phiên làm việc!
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(token)) {
            user.setRefreshToken(null);
            userRepository.save(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cảnh báo bảo mật: Refresh Token đã được sử dụng. Vui lòng đăng nhập lại.");
        }

        String newAccessToken = jwtService.generateToken(user.getUsername(), user.getRole(), user.getId());
        String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}
