package com.micro.service.crypto_monitor.business;

import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.model.FeatureToggle;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface FeatureToggleService {

    Mono<ApiResponseDTO<List<FeatureToggle>>> getAll();

    Mono<ApiResponseDTO<FeatureToggle>> getById(UUID id);

    Mono<ApiResponseDTO<FeatureToggle>> getByModuleName(String moduleName);

    Mono<ApiResponseDTO<Boolean>> isActive(String module);

    Mono<ApiResponseDTO<FeatureToggle>> create(FeatureToggle toggle);

    Mono<ApiResponseDTO<FeatureToggle>> activate(String moduleName);

    Mono<ApiResponseDTO<FeatureToggle>> deactivate(String moduleName);

    Mono<ApiResponseDTO<FeatureToggle>> update(UUID id, FeatureToggle toggle);

    Mono<ApiResponseDTO<Void>> delete(UUID id);
}
