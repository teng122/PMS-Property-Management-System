package com.smarthotel.booking_service.dto.external;

import lombok.Data;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private String role;
}
