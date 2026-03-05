package com.micro.service.crypto_monitor.controllers;

import com.micro.service.crypto_monitor.business.UserService;
import com.micro.service.crypto_monitor.dto.UserRequestDTO;
import com.micro.service.crypto_monitor.dto.UserResponseDTO;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${application.request.mappings}/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (solo ADMIN)")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserResponseDTO> create(@RequestBody UserRequestDTO dto) {
        System.out.println("🔥🔥🔥 CREATE CONTROLLER - DTO recibido:");
        System.out.println("   name: " + dto.getName());
        System.out.println("   lastname: " + dto.getLastname());
        System.out.println("   email: " + dto.getEmail());
        System.out.println("   username: " + dto.getUsername());
        System.out.println("   role: " + dto.getRole());
        System.out.println("   password: " + (dto.getPassword() != null ? "PROVIDED" : "NULL"));
        
        return userService.create(dto)
            .doOnSuccess(user -> System.out.println("✅ Usuario creado: " + user))
            .doOnError(error -> System.err.println("❌ Error en create: " + error.getMessage()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<UserResponseDTO> findAll() {
        return userService.findAll();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> delete(@PathVariable UUID id) {
        return userService.delete(id);
    }
}
