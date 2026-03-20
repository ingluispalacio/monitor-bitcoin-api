package com.micro.service.crypto_monitor.callservice;

import com.micro.service.crypto_monitor.config.ApiConfig;
import com.micro.service.crypto_monitor.dto.CoinGeckoPriceResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallServiceCoinGeckoHttp {

    private final ApiConfig apiConfig;

    @Value("${services.coingecko.base-url}")
    private String baseUrl;

    public Mono<BigDecimal> getBitcoinPrice() {

        log.info("COINGECKO - CONSULTA BTC - Inicio");

        return Mono.fromCallable(() -> {

            var api = apiConfig.getApi(CoinGeckoHttpClient.class, baseUrl);

            Call<CoinGeckoPriceResponseDTO> call =
                    api.getBitcoinPrice("bitcoin", "usd", false, false);

            Response<CoinGeckoPriceResponseDTO> response = call.execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("Error HTTP: " + response.code());
            }

            return response.body();

        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(this::parsePriceResponse)
        .doOnSuccess(price -> log.info("Precio BTC: ${}", price))
        .doOnError(error -> log.error("Error CoinGecko: {}", error.getMessage(), error));
    }

    private BigDecimal parsePriceResponse(CoinGeckoPriceResponseDTO response) {

        if (response == null || response.getBitcoin() == null) {
            throw new RuntimeException("Respuesta inválida de CoinGecko");
        }

        return response.getBitcoin().getUsd();
    }
}