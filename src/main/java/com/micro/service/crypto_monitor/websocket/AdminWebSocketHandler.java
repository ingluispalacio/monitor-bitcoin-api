package com.micro.service.crypto_monitor.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AdminWebSocketHandler implements WebSocketHandler {

    private final EventBus eventBus;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(
            eventBus.getEventStream()
                // Filtramos para que el admin no reciba precios de BTC si no quiere, 
                // o dejamos pasar todo lo relacionado a órdenes.
                .filter(event -> event.getType().toString().contains("ORDER"))
                .map(event -> {
                    try {
                        return session.textMessage(objectMapper.writeValueAsString(event));
                    } catch (Exception e) {
                        return session.textMessage("Error serializing event");
                    }
                })
        );
    }
}