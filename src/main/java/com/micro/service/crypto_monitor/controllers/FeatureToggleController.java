package com.micro.service.crypto_monitor.controllers;

import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.model.FeatureToggle;
import com.micro.service.crypto_monitor.repository.FeatureToggleRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${application.request.mappings}/features")
@RequiredArgsConstructor
@Tag(name = "Feature Toggles", description = "Endpoints para gestionar características del sistema")
public class FeatureToggleController {

    private final FeatureToggleRepository repository;

    // Obtener todos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public Mono<ResponseEntity<ApiResponseDTO<List<FeatureToggle>>>> getAll() {
        return repository.findAll()
                .collectList()
                .map(list -> ResponseEntity.ok(
                        ApiResponseDTO.<List<FeatureToggle>>builder()
                                .success(true)
                                .message("Lista de toggles obtenida correctamente")
                                .data(list)
                                .timestamp(LocalDateTime.now())
                                .build()
                ));
    }

    // Obtener por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponseDTO<FeatureToggle>>> getById(@PathVariable UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Toggle no encontrado")))
                .map(toggle -> ResponseEntity.ok(
                        ApiResponseDTO.<FeatureToggle>builder()
                                .success(true)
                                .message("Toggle encontrado")
                                .data(toggle)
                                .timestamp(LocalDateTime.now())
                                .build()
                ));
    }

    // Obtener por módulo
    @GetMapping("/module/{moduleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponseDTO<FeatureToggle>>> getByModuleName(@PathVariable String moduleName) {
        return repository.findByModuleName(moduleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Módulo no encontrado")))
                .map(toggle -> ResponseEntity.ok(
                        ApiResponseDTO.<FeatureToggle>builder()
                                .success(true)
                                .message("Módulo encontrado")
                                .data(toggle)
                                .timestamp(LocalDateTime.now())
                                .build()
                ));
    }

    // Verificar si está activo (público)
    @GetMapping("/module/{moduleName}/active")
    public Mono<ResponseEntity<ApiResponseDTO<Boolean>>> isActive(@PathVariable String moduleName) {
        return repository.findByModuleName(moduleName)
                .map(FeatureToggle::isActive)
                .defaultIfEmpty(false)
                .map(active -> ResponseEntity.ok(
                        ApiResponseDTO.<Boolean>builder()
                                .success(true)
                                .message("Estado del módulo obtenido")
                                .data(active)
                                .timestamp(LocalDateTime.now())
                                .build()
                ));
    }

    // Crear
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponseDTO<FeatureToggle>>> create(@RequestBody FeatureToggle toggle) {

        toggle.setId(UUID.randomUUID());

        return repository.existsByModuleName(toggle.getModuleName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("El módulo ya existe"));
                    }
                    return repository.save(toggle);
                })
                .map(saved -> ResponseEntity.ok(
                        ApiResponseDTO.<FeatureToggle>builder()
                                .success(true)
                                .message("Toggle creado correctamente")
                                .data(saved)
                                .timestamp(LocalDateTime.now())
                                .build()
                ));
    }

    // Activar
    @PutMapping("/{moduleName}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponseDTO<FeatureToggle>>> activate(@PathVariable String moduleName) {
        return repository.findByModuleName(moduleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Módulo no encontrado")))
                .flatMap(toggle -> {
                    toggle.setActive(true);
                    return repository.save(toggle);
                })
                .map(updated -> ResponseEntity.ok(
                        ApiResponseDTO.<FeatureToggle>builder()
                                .success(true)
                                .message("Módulo activado")
                                .data(updated)
                                .timestamp(LocalDateTime.now())
                                .build()
                ));
    }

    // Desactivar
    @PutMapping("/{moduleName}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponseDTO<FeatureToggle>>> deactivate(@PathVariable String moduleName) {
        return repository.findByModuleName(moduleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Módulo no encontrado")))
                .flatMap(toggle -> {
                    toggle.setActive(false);
                    return repository.save(toggle);
                })
                .map(updated -> ResponseEntity.ok(
                        ApiResponseDTO.<FeatureToggle>builder()
                                .success(true)
                                .message("Módulo desactivado")
                                .data(updated)
                                .timestamp(LocalDateTime.now())
                                .build()
                ));
    }

    // Actualizar
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponseDTO<FeatureToggle>>> update(@PathVariable UUID id,
                                                                       @RequestBody FeatureToggle toggle) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Toggle no encontrado")))
                .flatMap(existing -> {
                    existing.setActive(toggle.isActive());
                    existing.setModuleName(toggle.getModuleName());
                    return repository.save(existing);
                })
                .map(updated -> ResponseEntity.ok(
                        ApiResponseDTO.<FeatureToggle>builder()
                                .success(true)
                                .message("Toggle actualizado correctamente")
                                .data(updated)
                                .timestamp(LocalDateTime.now())
                                .build()
                ));
    }

    // Eliminar
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<ApiResponseDTO<Void>>> delete(@PathVariable UUID id) {
        return repository.deleteById(id)
                .then(Mono.just(ResponseEntity.ok(
                        ApiResponseDTO.<Void>builder()
                                .success(true)
                                .message("Toggle eliminado correctamente")
                                .data(null)
                                .timestamp(LocalDateTime.now())
                                .build()
                )));
    }
}