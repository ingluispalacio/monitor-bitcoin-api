package com.micro.service.crypto_monitor.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.micro.service.crypto_monitor.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {

    @Id
    private UUID id;

    private String name;
    private String lastname;
    private String email;
    private String username;
    private String password;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
}
