package com.micro.service.crypto_monitor.business.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.micro.service.crypto_monitor.business.UserService;
import com.micro.service.crypto_monitor.dto.UserRequestDTO;
import com.micro.service.crypto_monitor.dto.UserResponseDTO;
import com.micro.service.crypto_monitor.model.User;
import com.micro.service.crypto_monitor.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<UserResponseDTO> create(UserRequestDTO dto) {
        log.debug("CREATE SERVICE - Iniciando creación");

        // Validar campos obligatorios
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Username es requerido"));
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Email es requerido"));
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Password es requerido"));
        }
        if (dto.getRole() == null) {
            return Mono.error(new IllegalArgumentException("Role es requerido"));
        }

        return repository.findByUsername(dto.getUsername())
                .flatMap(user -> {
                    log.debug("Username ya existe: " + user.getUsername());
                    return Mono.error(new IllegalArgumentException("Username already exists"));
                })
                .switchIfEmpty(
                        repository.findByEmail(dto.getEmail())
                                .flatMap(user -> {
                                    log.debug("Email ya existe: " + user.getEmail());
                                    return Mono.error(new IllegalArgumentException("Email already exists"));
                                }))
                .then(
                        Mono.defer(() -> {
                            try {
                                log.debug("Creando nuevo usuario...");
                                User newUser = User.builder()
                                        .name(dto.getName())
                                        .lastname(dto.getLastname())
                                        .email(dto.getEmail())
                                        .username(dto.getUsername())
                                        .password(passwordEncoder.encode(dto.getPassword()))
                                        .role(dto.getRole())
                                        .active(dto.isActive())
                                        .build();

                                log.debug("Usuario construido con ID: " + newUser.getId());
                                return repository.save(newUser);
                            } catch (Exception e) {
                                log.error(" Error construyendo usuario: " + e.getMessage());
                                e.printStackTrace();
                                return Mono.error(e);
                            }
                        }))
                .map(user -> {
                    log.debug("✅ Usuario guardado en BD: " + user.getId());
                    return mapToDTO(user);
                })
                .doOnError(error -> {
                    log.error(" Error en create(): " + error.getMessage());
                    error.printStackTrace();
                });
    }

    @Override
    public Flux<UserResponseDTO> findAll() {
        return repository.findAll().map(this::mapToDTO);
    }

    @Override
    public Mono<UserResponseDTO> findById(UUID id) {
        return repository.findById(id).map(this::mapToDTO);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}