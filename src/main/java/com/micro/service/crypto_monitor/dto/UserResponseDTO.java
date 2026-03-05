package com.micro.service.crypto_monitor.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import com.micro.service.crypto_monitor.enums.UserRole;

@Data
@Builder
public class UserResponseDTO {

    private UUID id;
    private String name;
    private String lastname;
    private String email;
    private String username;
    private boolean active;
    private UserRole role;
    private LocalDateTime createdAt;
}