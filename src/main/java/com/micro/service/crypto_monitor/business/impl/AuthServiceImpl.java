package com.micro.service.crypto_monitor.business.impl;

import com.micro.service.crypto_monitor.business.AuthService;
import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.LoginRequestDTO;
import com.micro.service.crypto_monitor.dto.TokenResponseDTO;
import com.micro.service.crypto_monitor.model.User;
import com.micro.service.crypto_monitor.repository.UserRepository;
import com.micro.service.crypto_monitor.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<ApiResponseDTO<TokenResponseDTO>> login(LoginRequestDTO request) {

        log.info("SERVICIO AUTENTICACIÓN - LOGIN - Intento de inicio de sesión para usuario={}", request.getUsername());

        return userRepository.findByUsername(request.getUsername())

                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("SERVICIO AUTENTICACIÓN - LOGIN - Usuario no encontrado username={}", request.getUsername());
                    return Mono.error(new RuntimeException("Credenciales inválidas"));
                }))

                .flatMap(user -> validatePassword(user, request.getPassword()))

                .map(this::generateTokenResponse)

                .doOnNext(tokenResponse ->
                        log.info("SERVICIO AUTENTICACIÓN - LOGIN - Inicio de sesión exitoso usuario={} rol={}",
                                request.getUsername(), tokenResponse.getRole()))

                .map(tokenResponse -> ApiResponseDTO.<TokenResponseDTO>builder()
                        .success(true)
                        .message("Login exitoso")
                        .data(tokenResponse)
                        .timestamp(LocalDateTime.now())
                        .build())

                .onErrorResume(ex -> {

                    log.warn("SERVICIO AUTENTICACIÓN - LOGIN - Error de autenticación usuario={} error={}",
                            request.getUsername(), ex.getMessage());

                    return Mono.just(ApiResponseDTO.<TokenResponseDTO>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    private Mono<User> validatePassword(User user, String rawPassword) {

        log.debug("SERVICIO AUTENTICACIÓN - VALIDAR PASSWORD - Validando usuario={}", user.getUsername());

        if (!Boolean.TRUE.equals(user.getActive())) {

            log.warn("SERVICIO AUTENTICACIÓN - VALIDAR PASSWORD - Usuario inactivo username={}", user.getUsername());

            return Mono.error(new RuntimeException("Usuario inactivo"));
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {

            log.warn("SERVICIO AUTENTICACIÓN - VALIDAR PASSWORD - Contraseña incorrecta username={}", user.getUsername());

            return Mono.error(new RuntimeException("Credenciales inválidas"));
        }

        log.debug("SERVICIO AUTENTICACIÓN - VALIDAR PASSWORD - Credenciales válidas username={}", user.getUsername());

        return Mono.just(user);
    }

    private TokenResponseDTO generateTokenResponse(User user) {

        log.debug("SERVICIO AUTENTICACIÓN - TOKEN - Generando token para usuario={}", user.getUsername());

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole()
        );

        log.info("SERVICIO AUTENTICACIÓN - TOKEN - Token generado correctamente para usuario={}", user.getUsername());

        return TokenResponseDTO.builder()
                .token(token)
                .fullName(user.getName() + " " + user.getLastname())
                .role(user.getRole())
                .build();
    }
}