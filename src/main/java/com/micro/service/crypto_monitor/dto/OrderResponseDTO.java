package com.micro.service.crypto_monitor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.micro.service.crypto_monitor.enums.OrderStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponseDTO {
    private UUID id;
    private String clientName;
    private String cryptoName;
    private BigDecimal amount;
    private BigDecimal price;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
