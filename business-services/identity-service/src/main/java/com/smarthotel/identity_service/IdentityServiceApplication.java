package com.smarthotel.identity_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import com.smarthotel.common_shared.security.SharedSecurityConfig;

@SpringBootApplication
@EnableDiscoveryClient
@Import(SharedSecurityConfig.class)
public class IdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
