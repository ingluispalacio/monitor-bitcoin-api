package com.micro.service.crypto_monitor.callservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j // Agregamos logs para rastrear la respuesta
public class CallServicePriceHttp {

    private final WebClient coinGeckoWebClient;

    public Mono<BigDecimal> getBitcoinPrice() {
        return coinGeckoWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/simple/price")
                        .queryParam("ids", "bitcoin")
                        .queryParam("vs_currencies", "usd")
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        // 1. Obtenemos el mapa interno de "bitcoin"
                        Map<String, Object> bitcoinData = (Map<String, Object>) response.get("bitcoin");
                        
                        // 2. Extraemos el valor como Number (esto acepta Integer o Double)
                        Number priceValue = (Number) bitcoinData.get("usd");
                        
                        // 3. Convertimos a BigDecimal de forma segura
                        return BigDecimal.valueOf(priceValue.doubleValue());
                        
                    } catch (Exception e) {
                        log.error("❌ Error parseando la respuesta de CoinGecko: {}", response, e);
                        return BigDecimal.ZERO;
                    }
                })
                .doOnError(error -> log.error("❌ Error en la petición a CoinGecko: {}", error.getMessage()));
    }
}