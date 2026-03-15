package com.micro.service.crypto_monitor.business;

import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.UserRequestDTO;
import com.micro.service.crypto_monitor.dto.UserResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface UserService {

    Mono<ApiResponseDTO<UserResponseDTO>> create(UserRequestDTO dto);

    Mono<ApiResponseDTO<List<UserResponseDTO>>> findAll();

    Mono<ApiResponseDTO<UserResponseDTO>> findById(UUID id);

    Mono<ApiResponseDTO<Void>> delete(UUID id);
}