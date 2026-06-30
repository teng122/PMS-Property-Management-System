package com.smarthotel.identity_service.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private String role;
}
