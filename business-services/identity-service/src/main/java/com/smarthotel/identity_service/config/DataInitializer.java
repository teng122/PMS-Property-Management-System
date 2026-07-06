package com.smarthotel.identity_service.config;

import com.smarthotel.identity_service.entity.RoleName;
import com.smarthotel.identity_service.entity.User;
import com.smarthotel.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed default admin account if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .email("admin@smarthotel.com")
                    .role(RoleName.ROLE_ADMIN.name())
                    .status("ACTIVE")
                    .build();
            userRepository.save(admin);
            System.out.println("Default admin account seeded successfully: admin / admin123");
        }
    }
}
