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

        log.info("CLIENTE EXTERNO - COINGECKO - CONSULTA BTC - Iniciando solicitud del precio de Bitcoin");

        return Mono.fromCallable(() -> {

            log.info("CLIENTE EXTERNO - COINGECKO - CONSULTA BTC - Ejecutando petición HTTP");

            Response<CoinGeckoPriceResponseDTO> response = coinGeckoHttpClient
                    .getBitcoinPrice("bitcoin", "usd", false, false)
                    .execute();

            log.info("CLIENTE EXTERNO - COINGECKO - CONSULTA BTC - Respuesta HTTP recibida con código: {}", response.code());

            return response;

        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(Response::body)
        .doOnSuccess(body -> log.debug("CLIENTE EXTERNO - COINGECKO - CONSULTA BTC - Cuerpo de respuesta recibido"))
        .map(this::parsePriceResponse)
        .doOnSuccess(price ->
                log.info("CLIENTE EXTERNO - COINGECKO - CONSULTA BTC - Precio obtenido correctamente: ${}", price))
        .doOnError(error ->
                log.error("CLIENTE EXTERNO - COINGECKO - CONSULTA BTC - Error en la petición a CoinGecko: {}", error.getMessage(), error));
    }

    private BigDecimal parsePriceResponse(CoinGeckoPriceResponseDTO response) {

        log.debug("CLIENTE EXTERNO - COINGECKO - PARSE RESPONSE - Procesando respuesta de CoinGecko");

        if (response == null || response.getBitcoin() == null) {
            log.error("CLIENTE EXTERNO - COINGECKO - PARSE RESPONSE - Respuesta inválida recibida de CoinGecko");
            return BigDecimal.ZERO;
        }

        try {

            BigDecimal price = response.getBitcoin().getUsd();

            log.info("CLIENTE EXTERNO - COINGECKO - PARSE RESPONSE - Precio BTC parseado correctamente: ${}", price);

            return price;

        } catch (Exception e) {

            log.error("CLIENTE EXTERNO - COINGECKO - PARSE RESPONSE - Error parseando la respuesta: {}", response, e);

            return BigDecimal.ZERO;
        }
    }
}