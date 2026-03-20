package com.micro.service.crypto_monitor.business.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.micro.service.crypto_monitor.business.UserService;
import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.UserRequestDTO;
import com.micro.service.crypto_monitor.dto.UserResponseDTO;
import com.micro.service.crypto_monitor.mapper.UserMapper;
import com.micro.service.crypto_monitor.model.User;
import com.micro.service.crypto_monitor.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public Mono<ApiResponseDTO<UserResponseDTO>> create(UserRequestDTO dto) {

        log.info("SERVICIO USUARIO - CREAR - username={}", dto.getUsername());

        return repository.findByUsername(dto.getUsername())
                .flatMap(user -> {
                    log.warn("Username ya existe: {}", dto.getUsername());
                    return Mono.error(new IllegalArgumentException("Username ya existe"));
                })
                .switchIfEmpty(
                        repository.findByEmail(dto.getEmail())
                                .flatMap(user -> {
                                    log.warn("Email ya existe: {}", dto.getEmail());
                                    return Mono.error(new IllegalArgumentException("Email ya existe"));
                                }))
                .then(Mono.defer(() -> {

                    User newUser = userMapper.toEntity(dto);

                    newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
                    newUser.setCreatedAt(LocalDateTime.now());

                    return repository.save(newUser);
                }))
                .doOnSuccess(user -> log.info("Usuario creado id={}", user.getId()))
                .map(userMapper::toDTO)
                .map(response -> ApiResponseDTO.<UserResponseDTO>builder()
                        .success(true)
                        .message("Usuario creado correctamente")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build())
                .onErrorResume(ex -> Mono.just(ApiResponseDTO.<UserResponseDTO>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()));
    }

    @Override
    public Mono<ApiResponseDTO<List<UserResponseDTO>>> findAll() {

        log.info("SERVICIO USUARIO - CONSULTAR TODOS - Obteniendo lista de usuarios");

        return repository.findAll()
                .map(userMapper::toDTO)
                .collectList()
                .doOnSuccess(list -> log.info("CONSULTAR USUARIOS - Se obtuvieron {} usuarios", list.size()))
                .map(list -> ApiResponseDTO.<List<UserResponseDTO>>builder()
                        .success(true)
                        .message("Usuarios obtenidos correctamente")
                        .data(list)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnError(error -> log.error("CONSULTAR USUARIOS - Error al obtener usuarios: {}", error.getMessage(),
                        error))
                .onErrorResume(ex -> Mono.just(ApiResponseDTO.<List<UserResponseDTO>>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()));
    }

    @Override
    public Mono<ApiResponseDTO<UserResponseDTO>> findById(UUID id) {

        log.info("SERVICIO USUARIO - BUSCAR POR ID - Buscando usuario con ID: {}", id);

        return repository.findById(id)
                .map(userMapper::toDTO)
                .doOnSuccess(user -> log.info("BUSCAR USUARIO - Usuario encontrado con ID: {}", id))
                .map(dto -> ApiResponseDTO.<UserResponseDTO>builder()
                        .success(true)
                        .message("Usuario encontrado")
                        .data(dto)
                        .timestamp(LocalDateTime.now())
                        .build())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("BUSCAR USUARIO - Usuario no encontrado con ID: {}", id);
                    return Mono.just(ApiResponseDTO.<UserResponseDTO>builder()
                            .success(false)
                            .message("Usuario no encontrado")
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                }))
                .doOnError(
                        error -> log.error("BUSCAR USUARIO - Error al buscar usuario: {}", error.getMessage(), error))
                .onErrorResume(ex -> Mono.just(ApiResponseDTO.<UserResponseDTO>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()));
    }

    @Override
    public Mono<ApiResponseDTO<Void>> delete(UUID id) {

        log.info("SERVICIO USUARIO - ELIMINAR - Intentando eliminar usuario con ID: {}", id);

        return repository.deleteById(id)
                .then(Mono.fromSupplier(() -> {
                    log.info("ELIMINAR USUARIO - Usuario eliminado correctamente con ID: {}", id);
                    return ApiResponseDTO.<Void>builder()
                            .success(true)
                            .message("Usuario eliminado correctamente")
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build();
                }))
                .doOnError(error -> log.error("ELIMINAR USUARIO - Error al eliminar usuario {}: {}", id,
                        error.getMessage(), error))
                .onErrorResume(ex -> Mono.just(ApiResponseDTO.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()));
    }
}