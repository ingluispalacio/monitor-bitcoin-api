package com.micro.service.crypto_monitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.coingecko.base-url}")
    private String coingeckoBaseUrl;

    @Bean
    public WebClient coinGeckoWebClient() {
        return WebClient.builder()
                .baseUrl(coingeckoBaseUrl)
                .build();
    }
}