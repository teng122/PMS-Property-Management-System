package com.fpt.s1identityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// @EnableDiscoveryClient // Bạn có thể tạm thời comment dòng này lại nếu chưa bật Eureka Server
public class S1IdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(S1IdentityServiceApplication.class, args);
    }
}