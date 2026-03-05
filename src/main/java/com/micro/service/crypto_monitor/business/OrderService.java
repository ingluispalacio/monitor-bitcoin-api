package com.micro.service.crypto_monitor.business;

import java.math.BigDecimal;
import java.util.UUID;

import com.micro.service.crypto_monitor.enums.OrderStatus;
import com.micro.service.crypto_monitor.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<Order> createOrder(String clientName, String crypto, String amount, BigDecimal price,
            BigDecimal total);

    Mono<Order> updateOrderStatus(UUID orderId, String status);

    Mono<Order> approveOrder(UUID orderId);  

    Mono<Order> rejectOrder(UUID orderId);
    
    Flux<Order> getAllOrders();
    
    Flux<Order> getOrdersByClientName(String username);
    
    Flux<Order> getOrdersByStatus(OrderStatus status);
}