package com.micro.service.crypto_monitor.business.impl;

import com.micro.service.crypto_monitor.business.PriceService;
import com.micro.service.crypto_monitor.callservice.CallServiceCoinGeckoHttp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class PriceServiceImpl implements PriceService {

    private final CallServiceCoinGeckoHttp callServiceCoinGeckoHttp;

    @Override
    public Mono<BigDecimal> getBitcoinPrice() {

        log.info("SERVICIO PRECIO - BITCOIN - Iniciando consulta del precio de Bitcoin");

        return callServiceCoinGeckoHttp
                .getBitcoinPrice();
    }
}