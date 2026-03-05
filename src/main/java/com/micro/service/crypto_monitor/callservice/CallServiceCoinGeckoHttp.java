package com.micro.service.crypto_monitor.callservice;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.micro.service.crypto_monitor.dto.CoinGeckoPriceResponseDTO;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import retrofit2.Response;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class CallServiceCoinGeckoHttp {

    private final CoinGeckoHttpClient coinGeckoHttpClient;

    public Mono<BigDecimal> getBitcoinPrice() {

        return Mono.fromCallable(() ->
                coinGeckoHttpClient
                        .getBitcoinPrice("bitcoin", "usd", false, false)
                        .execute()
        )
        .subscribeOn(Schedulers.boundedElastic())
        .map(Response::body)
        .map(this::parsePriceResponse)
        .doOnError(error ->
                log.error("❌ Error en petición a CoinGecko: {}", error.getMessage(), error));
    }

    private BigDecimal parsePriceResponse(CoinGeckoPriceResponseDTO response) {
        
        if (response == null || response.getBitcoin() == null) {
            log.error("❌ Respuesta inválida de CoinGecko");
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal price = response.getBitcoin().getUsd();
            log.info("💰 Precio BTC obtenido: ${}", price);
            return price;
            
        } catch (Exception e) {
            log.error("❌ Error parseando respuesta: {}", response, e);
            return BigDecimal.ZERO;
        }
    }
}