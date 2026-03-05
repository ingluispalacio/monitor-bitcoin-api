package com.micro.service.crypto_monitor.business;

import com.micro.service.crypto_monitor.dto.UserRequestDTO;
import com.micro.service.crypto_monitor.dto.UserResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService {

    Mono<UserResponseDTO> create(UserRequestDTO dto);

    Flux<UserResponseDTO> findAll();

    Mono<UserResponseDTO> findById(UUID id);

    Mono<Void> delete(UUID id);
}