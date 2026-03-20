package com.micro.service.crypto_monitor.controllers;

import com.micro.service.crypto_monitor.business.UserService;
import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.UserRequestDTO;
import com.micro.service.crypto_monitor.dto.UserResponseDTO;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${application.request.mappings}/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (solo ADMIN)")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<UserResponseDTO>> create(@Valid @RequestBody UserRequestDTO dto) {
        log.info("Inicio controller create User con request -> {}", dto);
        return userService.create(dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<List<UserResponseDTO>>> findAll() {
        log.info("Inicio controller findAll User");
        return userService.findAll();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<Void>> delete(@PathVariable UUID id) {
        log.info("Inicio controller delete User, id={}", id);
        return userService.delete(id);
    }
}
