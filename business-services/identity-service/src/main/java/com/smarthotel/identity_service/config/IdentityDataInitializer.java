package com.smarthotel.identity_service.config;

import com.smarthotel.identity_service.entity.RoleName;
import com.smarthotel.identity_service.entity.User;
import com.smarthotel.identity_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class IdentityDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUser(
                "admin",
                "admin123",
                "System Administrator",
                "admin@smarthotel.com",
                RoleName.ROLE_ADMIN
        );

        // Staff
        createUser(
                "staff1",
                "123456",
                "Staff One",
                "staff1@smarthotel.com",
                RoleName.ROLE_STAFF
        );

        createUser(
                "staff2",
                "123456",
                "Staff Two",
                "staff2@smarthotel.com",
                RoleName.ROLE_STAFF
        );

        // Receptionists
        createUser(
                "recept1",
                "123456",
                "Receptionist One",
                "recept1@smarthotel.com",
                RoleName.ROLE_RECEPTIONIST
        );

        createUser(
                "recept2",
                "123456",
                "Receptionist Two",
                "recept2@smarthotel.com",
                RoleName.ROLE_RECEPTIONIST
        );

        // Customers
        createUser(
                "guest1",
                "123456",
                "Guest One",
                "guest1@smarthotel.com",
                RoleName.ROLE_CUSTOMER
        );

        createUser(
                "guest2",
                "123456",
                "Guest Two",
                "guest2@smarthotel.com",
                RoleName.ROLE_CUSTOMER
        );

        createUser(
                "guest3",
                "123456",
                "Guest Three",
                "guest3@smarthotel.com",
                RoleName.ROLE_CUSTOMER
        );
    }

    private void createUser(
            String username,
            String password,
            String fullName,
            String email,
            RoleName role
    ) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .fullName(fullName)
                    .email(email)
                    .role(role.name())
                    .status("ACTIVE")
                    .build();

            userRepository.save(user);
            System.out.println("Created user: " + username);
        }
    }
}