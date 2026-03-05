package com.micro.service.crypto_monitor.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String username;
    private String password;
}