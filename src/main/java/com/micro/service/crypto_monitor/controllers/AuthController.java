package com.micro.service.crypto_monitor.controllers;

import com.micro.service.crypto_monitor.business.AuthService;
import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.LoginRequestDTO;
import com.micro.service.crypto_monitor.dto.TokenResponseDTO;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${application.request.mappings}/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para login y registro de usuarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Mono<ApiResponseDTO<TokenResponseDTO>> login(
            @RequestBody Mono<LoginRequestDTO> requestMono) {

        return requestMono
            .doOnNext(req -> log.info("Inicio controller login con request -> {}", req))
            .flatMap(authService::login);
    }
}