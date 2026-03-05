package com.micro.service.crypto_monitor.controllers;

import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.LoginRequestDTO;
import com.micro.service.crypto_monitor.dto.TokenResponseDTO;
import com.micro.service.crypto_monitor.model.User;
import com.micro.service.crypto_monitor.repository.UserRepository;
import com.micro.service.crypto_monitor.security.JwtUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${application.request.mappings}/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para login y registro de usuarios")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponseDTO<TokenResponseDTO>>> login(
            @RequestBody Mono<LoginRequestDTO> requestMono) {

         return requestMono
            .flatMap(request ->

                    userRepository.findByUsername(request.getUsername())
                            .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
                            .flatMap(user -> validatePassword(user, request.getPassword()))
                            .map(this::generateTokenResponse)
            )
            .map(tokenResponse ->

                    ResponseEntity.ok(
                            ApiResponseDTO.<TokenResponseDTO>builder()
                                    .success(true)
                                    .message("Login successful")
                                    .data(tokenResponse)
                                    .timestamp(java.time.LocalDateTime.now())
                                    .build()
                    )
            )
            .onErrorResume(ex ->
                    Mono.just(
                            ResponseEntity.status(401).body(
                                    ApiResponseDTO.<TokenResponseDTO>builder()
                                            .success(false)
                                            .message(ex.getMessage())
                                            .data(null)
                                            .timestamp(java.time.LocalDateTime.now())
                                            .build()
                            )
                    )
            );
    }

    private Mono<User> validatePassword(User user, String rawPassword) {

        if (!Boolean.TRUE.equals(user.getActive())) {
            return Mono.error(new RuntimeException("User inactive"));
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return Mono.error(new RuntimeException("Invalid credentials"));
        }

        return Mono.just(user);
    }

    private TokenResponseDTO generateTokenResponse(User user) {

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole()
        );
         return TokenResponseDTO.builder()
            .token(token)
            .fullName(user.getName() + " " + user.getLastname())   // asegúrate que exista este campo
            .role(user.getRole())
            .build();

    }
}