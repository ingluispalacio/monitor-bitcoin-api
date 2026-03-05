package com.micro.service.crypto_monitor.config;


import com.micro.service.crypto_monitor.websocket.AdminWebSocketHandler;
import com.micro.service.crypto_monitor.websocket.ClientWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    private final ClientWebSocketHandler clientHandler;
    private final AdminWebSocketHandler adminHandler;

    @Bean
    public HandlerMapping webSocketMapping() {

        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/client", clientHandler);
        map.put("/ws/admin", adminHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(-1);
        mapping.setUrlMap(map);

        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}