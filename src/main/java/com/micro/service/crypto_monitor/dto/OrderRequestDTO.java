package com.micro.service.crypto_monitor.dto;

import java.math.BigDecimal;

// import com.micro.service.crypto_monitor.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class OrderRequestDTO {

    private String clientName;
    private String cryptoName;
    private BigDecimal amount;
    private BigDecimal price;
    private BigDecimal total;
}
