package com.micro.service.crypto_monitor.business;

import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.FeatureToggleDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface FeatureToggleService {

    Mono<ApiResponseDTO<List<FeatureToggleDTO>>> getAll();

    Mono<ApiResponseDTO<FeatureToggleDTO>> getById(UUID id);

    Mono<ApiResponseDTO<FeatureToggleDTO>> getByModuleName(String moduleName);

    Mono<ApiResponseDTO<Boolean>> isActive(String module);

    Mono<ApiResponseDTO<FeatureToggleDTO>> create(FeatureToggleDTO toggle);

    Mono<ApiResponseDTO<FeatureToggleDTO>> activate(String moduleName);

    Mono<ApiResponseDTO<FeatureToggleDTO>> deactivate(String moduleName);

    Mono<ApiResponseDTO<FeatureToggleDTO>> update(UUID id, FeatureToggleDTO toggle);

    Mono<ApiResponseDTO<Void>> delete(UUID id);
}
