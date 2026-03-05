package com.micro.service.crypto_monitor.dto;

import java.util.UUID;

import com.micro.service.crypto_monitor.enums.EventType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketEventDTO {
    private EventType type;
    private UUID orderId;
    private String message;
    private Object data;
}
