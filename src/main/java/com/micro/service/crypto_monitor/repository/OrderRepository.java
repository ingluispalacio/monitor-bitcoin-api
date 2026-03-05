package com.micro.service.crypto_monitor.repository;

import com.micro.service.crypto_monitor.enums.OrderStatus;
import com.micro.service.crypto_monitor.model.Order;

import reactor.core.publisher.Flux;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {
   
   
    Flux<Order> findByIdOrderByCreatedAtDesc(UUID id);
    Flux<Order> findByClientNameOrderByCreatedAtDesc(String clientName);
    
    Flux<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);
    
    Flux<Order> findAllByOrderByCreatedAtDesc();
    
    Flux<Order> findByClientNameAndStatus(String clientName, OrderStatus status);
}
