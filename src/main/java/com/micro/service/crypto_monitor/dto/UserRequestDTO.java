package com.micro.service.crypto_monitor.dto;
import com.micro.service.crypto_monitor.enums.UserRole;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String name;
    private String lastname;
    private String email;
    private String username;
    private String password;
    private UserRole role;
    private boolean active;
}
