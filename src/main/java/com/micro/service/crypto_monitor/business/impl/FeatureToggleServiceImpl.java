package com.micro.service.crypto_monitor.business.impl;

import com.micro.service.crypto_monitor.business.FeatureToggleService;
import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.model.FeatureToggle;
import com.micro.service.crypto_monitor.repository.FeatureToggleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FeatureToggleServiceImpl implements FeatureToggleService {

    private final FeatureToggleRepository featureToggleRepository;

    @Override
    public Mono<ApiResponseDTO<List<FeatureToggle>>> getAll() {
        log.info("FeatureToggleService getAll start");
        return featureToggleRepository.findAll().collectList()
                .map(list -> ApiResponseDTO.<List<FeatureToggle>>builder()
                        .success(true)
                        .message("Lista de toggles obtenida correctamente")
                        .data(list)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("FeatureToggleService getAll success, count={}", resp.getData().size()))
                .doOnError(err -> log.error("FeatureToggleService getAll error", err));
    }

    @Override
    public Mono<ApiResponseDTO<FeatureToggle>> getById(UUID id) {
        log.info("Inicio service getById FeatureToggle, id={}", id);
        return featureToggleRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Toggle no encontrado")))
                .map(toggle -> ApiResponseDTO.<FeatureToggle>builder()
                        .success(true)
                        .message("Toggle encontrado")
                        .data(toggle)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("getById éxito, id={}, active={}", id, resp.getData().isActive()))
                .onErrorResume(ex -> {
                    log.error("getById error id={}: {}", id, ex.getMessage());
                    return Mono.just(ApiResponseDTO.<FeatureToggle>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<FeatureToggle>> getByModuleName(String moduleName) {
        log.info("Inicio service getByModuleName FeatureToggle, moduleName={}", moduleName);
        return featureToggleRepository.findByModuleName(moduleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Módulo no encontrado")))
                .map(toggle -> ApiResponseDTO.<FeatureToggle>builder()
                        .success(true)
                        .message("Módulo encontrado")
                        .data(toggle)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("getByModuleName éxito: moduleName={}, active={}", moduleName, resp.getData().isActive()))
                .onErrorResume(ex -> {
                    log.error("getByModuleName error moduleName={}: {}", moduleName, ex.getMessage());
                    return Mono.just(ApiResponseDTO.<FeatureToggle>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<Boolean>> isActive(String module) {
        log.info("Inicio service isActive FeatureToggle, module={}", module);
        return featureToggleRepository
                .findByModuleName(module)
                .map(FeatureToggle::isActive)
                .defaultIfEmpty(false)
                .map(active -> ApiResponseDTO.<Boolean>builder()
                        .success(true)
                        .message("Estado del módulo obtenido")
                        .data(active)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("isActive result module={}, active={}", module, resp.getData()));
    }

    @Override
    public Mono<ApiResponseDTO<FeatureToggle>> create(FeatureToggle toggle) {
        toggle.setId(UUID.randomUUID());
        return featureToggleRepository.existsByModuleName(toggle.getModuleName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("El módulo ya existe"));
                    }
                    return featureToggleRepository.save(toggle);
                })
                .map(saved -> ApiResponseDTO.<FeatureToggle>builder()
                        .success(true)
                        .message("Toggle creado correctamente")
                        .data(saved)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("create éxito moduleName={}", toggle.getModuleName()))
                .onErrorResume(ex -> {
                    log.error("create error moduleName={} - {}", toggle.getModuleName(), ex.getMessage());
                    return Mono.just(ApiResponseDTO.<FeatureToggle>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<FeatureToggle>> activate(String moduleName) {
        return featureToggleRepository.findByModuleName(moduleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Módulo no encontrado")))
                .flatMap(toggle -> {
                    toggle.setActive(true);
                    return featureToggleRepository.save(toggle);
                })
                .map(updated -> ApiResponseDTO.<FeatureToggle>builder()
                        .success(true)
                        .message("Módulo activado")
                        .data(updated)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("activate éxito moduleName={}", moduleName))
                .onErrorResume(ex -> {
                    log.error("activate error moduleName={} - {}", moduleName, ex.getMessage());
                    return Mono.just(ApiResponseDTO.<FeatureToggle>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<FeatureToggle>> deactivate(String moduleName) {
        return featureToggleRepository.findByModuleName(moduleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Módulo no encontrado")))
                .flatMap(toggle -> {
                    toggle.setActive(false);
                    return featureToggleRepository.save(toggle);
                })
                .map(updated -> ApiResponseDTO.<FeatureToggle>builder()
                        .success(true)
                        .message("Módulo desactivado")
                        .data(updated)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("deactivate éxito moduleName={}", moduleName))
                .onErrorResume(ex -> {
                    log.error("deactivate error moduleName={} - {}", moduleName, ex.getMessage());
                    return Mono.just(ApiResponseDTO.<FeatureToggle>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<FeatureToggle>> update(UUID id, FeatureToggle toggle) {
        return featureToggleRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Toggle no encontrado")))
                .flatMap(existing -> {
                    existing.setActive(toggle.isActive());
                    existing.setModuleName(toggle.getModuleName());
                    return featureToggleRepository.save(existing);
                })
                .map(updated -> ApiResponseDTO.<FeatureToggle>builder()
                        .success(true)
                        .message("Toggle actualizado correctamente")
                        .data(updated)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("update éxito id={} module={} active={}", id, resp.getData().getModuleName(), resp.getData().isActive()))
                .onErrorResume(ex -> {
                    log.error("update error id={} - {}", id, ex.getMessage());
                    return Mono.just(ApiResponseDTO.<FeatureToggle>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<Void>> delete(UUID id) {
        return featureToggleRepository.deleteById(id)
                .then(Mono.just(ApiResponseDTO.<Void>builder()
                        .success(true)
                        .message("Toggle eliminado correctamente")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()))
                .doOnSuccess(resp -> log.info("delete éxito id={}", id))
                .onErrorResume(ex -> {
                    log.error("delete error id={} - {}", id, ex.getMessage());
                    return Mono.just(ApiResponseDTO.<Void>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }
}
