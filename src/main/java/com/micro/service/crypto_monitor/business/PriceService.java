package com.micro.service.crypto_monitor.business;

import java.math.BigDecimal;

import reactor.core.publisher.Mono;

public interface PriceService {
    
     Mono<BigDecimal> getBitcoinPrice();
    
} 