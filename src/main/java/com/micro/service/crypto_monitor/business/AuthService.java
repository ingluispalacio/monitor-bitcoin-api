package com.micro.service.crypto_monitor.business;

import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.LoginRequestDTO;
import com.micro.service.crypto_monitor.dto.TokenResponseDTO;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<ApiResponseDTO<TokenResponseDTO>> login(LoginRequestDTO request);
}