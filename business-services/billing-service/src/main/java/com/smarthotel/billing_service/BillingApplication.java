package com.smarthotel.billing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.context.annotation.Import;
import com.smarthotel.common_shared.security.SharedSecurityConfig;

@SpringBootApplication
@EnableFeignClients
@Import(SharedSecurityConfig.class)
public class BillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingApplication.class, args);
    }
}


