package com.micro.service.crypto_monitor.business.impl;

import org.springframework.stereotype.Service;

import com.micro.service.crypto_monitor.business.FeatureToggleService;
import com.micro.service.crypto_monitor.repository.FeatureToggleRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FeatureToggleServiceImpl implements FeatureToggleService {

    private final FeatureToggleRepository featureToggleRepository;

    @Override
    public Mono<Boolean> isActive(String module) {

        return featureToggleRepository
                .findByModuleName(module)
                .map(feature -> feature.isActive())
                .defaultIfEmpty(false);
    }
}