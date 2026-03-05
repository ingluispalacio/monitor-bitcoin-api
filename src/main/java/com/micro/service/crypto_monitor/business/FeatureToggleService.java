package com.micro.service.crypto_monitor.business;

import reactor.core.publisher.Mono;

public interface FeatureToggleService {

    Mono<Boolean> isActive(String module);
}
