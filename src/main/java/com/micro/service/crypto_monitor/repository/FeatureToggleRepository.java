package com.micro.service.crypto_monitor.repository;

import com.micro.service.crypto_monitor.model.FeatureToggle;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface FeatureToggleRepository extends ReactiveCrudRepository<FeatureToggle, UUID> {
    
    // Buscar por nombre del módulo
    Mono<FeatureToggle> findByModuleName(String moduleName);
    
    // Verificar si existe un módulo
    Mono<Boolean> existsByModuleName(String moduleName);
}