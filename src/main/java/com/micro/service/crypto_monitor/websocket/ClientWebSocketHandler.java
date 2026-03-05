package com.micro.service.crypto_monitor.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.service.crypto_monitor.enums.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
public class ClientWebSocketHandler implements WebSocketHandler {

    private final EventBus eventBus;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(
            eventBus.getEventStream()
                .filter(event -> event.getType() == EventType.PRICE_UPDATE)
                .map(event -> {
                    try {
                        return session.textMessage(objectMapper.writeValueAsString(event));
                    } catch (Exception e) {
                        return session.textMessage("Error");
                    }
                })
        );
    }
}