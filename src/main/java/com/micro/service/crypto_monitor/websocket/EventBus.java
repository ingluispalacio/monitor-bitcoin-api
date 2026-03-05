package com.micro.service.crypto_monitor.websocket;

import com.micro.service.crypto_monitor.dto.WebSocketEventDTO;
import com.micro.service.crypto_monitor.enums.EventType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class EventBus {

    // multicast(): permite que muchos suscriptores vean el mismo evento
    // directBestEffort(): si un suscriptor es lento, no detiene a los demás
    private final Sinks.Many<WebSocketEventDTO> bus = Sinks.many().multicast().directBestEffort();

    public void publish(EventType type, Object data) {
        WebSocketEventDTO event = WebSocketEventDTO.builder()
                .type(type)
                .data(data)
                .build();
        bus.tryEmitNext(event);
    }

    public Flux<WebSocketEventDTO> getEventStream() {
        return bus.asFlux();
    }
}