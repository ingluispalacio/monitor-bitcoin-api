package com.micro.service.crypto_monitor.controllers;

import com.micro.service.crypto_monitor.business.FeatureToggleService;
import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.FeatureToggleDTO;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${application.request.mappings}/features")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feature Toggles", description = "Endpoints para gestionar características del sistema")
public class FeatureToggleController {

    private final FeatureToggleService featureToggleService;

    // Obtener todos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public Mono<ApiResponseDTO<List<FeatureToggleDTO>>> getAll() {
        log.info("Inicio controller getAll FeatureToggle");
        return featureToggleService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<FeatureToggleDTO>> getById(@PathVariable UUID id) {
        log.info("Inicio controller getById FeatureToggle, id={}", id);
        return featureToggleService.getById(id);
    }


    @GetMapping("/module/{moduleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<FeatureToggleDTO>> getByModuleName(@PathVariable String moduleName) {
        log.info("Inicio controller getByModuleName FeatureToggle, moduleName={}", moduleName);
        return featureToggleService.getByModuleName(moduleName);
    }

    // Verificar si está activo (público)
    @GetMapping("/module/{moduleName}/active")
    public Mono<ApiResponseDTO<Boolean>> isActive(@PathVariable String moduleName) {
        log.info("Inicio controller isActive FeatureToggle, moduleName={}", moduleName);
        return featureToggleService.isActive(moduleName);
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<FeatureToggleDTO>> create(@RequestBody FeatureToggleDTO toggle) {
        log.info("Inicio controller create FeatureToggle con request -> {}", toggle);
        return featureToggleService.create(toggle);
    }

    @PutMapping("/{moduleName}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<FeatureToggleDTO>> activate(@PathVariable String moduleName) {
        log.info("Inicio controller activate FeatureToggle, moduleName={}", moduleName);
        return featureToggleService.activate(moduleName);
    }


    @PutMapping("/{moduleName}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<FeatureToggleDTO>> deactivate(@PathVariable String moduleName) {
        log.info("Inicio controller deactivate FeatureToggle, moduleName={}", moduleName);
        return featureToggleService.deactivate(moduleName);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<FeatureToggleDTO>> update(@PathVariable UUID id,
                                                       @RequestBody FeatureToggleDTO toggle) {
        log.info("Inicio controller update FeatureToggle, id={}, request-> {}", id, toggle);
        return featureToggleService.update(id, toggle);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<Void>> delete(@PathVariable UUID id) {
        log.info("Inicio controller delete FeatureToggle, id={}", id);
        return featureToggleService.delete(id);
    }
}