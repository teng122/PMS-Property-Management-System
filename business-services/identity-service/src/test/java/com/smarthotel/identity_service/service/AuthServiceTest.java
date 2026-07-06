package com.smarthotel.identity_service.service;

import com.smarthotel.identity_service.dto.RegisterRequest;
import com.smarthotel.identity_service.dto.UserResponse;
import com.smarthotel.identity_service.entity.User;
import com.smarthotel.identity_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldForceRoleToCustomer() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("customer1");
        req.setPassword("pwd");
        req.setFullName("Customer One");
        req.setEmail("customer1@gmail.com");
        req.setRole("ADMIN"); // Attacker tries to register as ADMIN

        when(userRepository.existsByUsername("customer1")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("encodedPwd");

        User userToSave = User.builder()
                .username("customer1")
                .password("encodedPwd")
                .fullName("Customer One")
                .email("customer1@gmail.com")
                .role("ROLE_CUSTOMER")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(userToSave);

        UserResponse resp = authService.register(req);

        assertThat(resp.getRole()).isEqualTo("ROLE_CUSTOMER");
    }

    @Test
    void registerByAdmin_shouldStandardizeAndAcceptValidRole() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("staff1");
        req.setPassword("pwd");
        req.setFullName("Staff One");
        req.setRole("staff"); // Lowercase, no prefix

        when(userRepository.existsByUsername("staff1")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("encodedPwd");

        User userToSave = User.builder()
                .username("staff1")
                .password("encodedPwd")
                .fullName("Staff One")
                .role("ROLE_STAFF")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(userToSave);

        UserResponse resp = authService.registerByAdmin(req);

        assertThat(resp.getRole()).isEqualTo("ROLE_STAFF");
    }

    @Test
    void registerByAdmin_shouldThrowExceptionForInvalidRole() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("hacker");
        req.setPassword("pwd");
        req.setRole("INVALID_ROLE");

        when(userRepository.existsByUsername("hacker")).thenReturn(false);

        assertThatThrownBy(() -> authService.registerByAdmin(req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Quyền (role) không hợp lệ");
    }
}
