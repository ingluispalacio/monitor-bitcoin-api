package com.micro.service.crypto_monitor.business.impl;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import com.micro.service.crypto_monitor.business.FeatureToggleService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FeatureToggleServiceImpl implements FeatureToggleService {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Boolean> isActive(String module) {
        return databaseClient
                .sql("SELECT active FROM feature_toggle WHERE module_name = :module")
                .bind("module", module)
                .map(row -> row.get("active", Boolean.class))
                .one()
                .defaultIfEmpty(false);
    }
}