package com.micro.service.crypto_monitor.config;

import com.micro.service.crypto_monitor.dto.WebSocketEventDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class SinkConfig {

    @Bean
    public Sinks.Many<WebSocketEventDTO> orderSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}