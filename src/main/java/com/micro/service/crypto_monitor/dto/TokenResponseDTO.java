package com.micro.service.crypto_monitor.dto;

import com.micro.service.crypto_monitor.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TokenResponseDTO {
    private String token;
    private String fullName;
    private UserRole role;
}
