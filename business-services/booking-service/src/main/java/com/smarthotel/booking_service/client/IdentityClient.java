package com.smarthotel.booking_service.client;

import com.smarthotel.booking_service.dto.external.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "identity-service")
public interface IdentityClient {

    @GetMapping("/api/auth/users/{id}")
    UserDto getUserById(@PathVariable("id") UUID id);
}
