package com.smarthotel.amenities_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.context.annotation.Import;
import com.smarthotel.common_shared.security.SharedSecurityConfig;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@Import(SharedSecurityConfig.class)
public class AmenitiesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmenitiesServiceApplication.class, args);
	}

}

