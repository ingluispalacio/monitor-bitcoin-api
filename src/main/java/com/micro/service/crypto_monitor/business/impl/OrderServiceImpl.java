package com.micro.service.crypto_monitor.business.impl;

import com.micro.service.crypto_monitor.business.OrderService;
import com.micro.service.crypto_monitor.enums.EventType;
import com.micro.service.crypto_monitor.enums.OrderStatus;
import com.micro.service.crypto_monitor.model.Order;
import com.micro.service.crypto_monitor.repository.OrderRepository;
import com.micro.service.crypto_monitor.websocket.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final EventBus eventBus; // Usamos el bus para desacoplar

    @Override
    public Mono<Order> createOrder(String clientName, String crypto, String amount, BigDecimal price,
            BigDecimal total) {
       
        BigDecimal amt = new BigDecimal(amount);

        Order order = Order.builder()
                .clientName(clientName)
                .cryptoName(crypto)
                .amount(amt)
                .price(price) 
                .total(total) 
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(order)
                .doOnSuccess(savedOrder -> {
                    log.info("Orden {} creada para el cliente {} con precio ${}",
                            savedOrder.getId(), clientName, price);
                    // Notificamos al bus para que el Admin Dashboard vea la nueva orden
                    eventBus.publish(EventType.ORDER_CREATED, savedOrder);
                })
                .doOnError(e -> log.error("Error al guardar la orden: {}", e.getMessage()));
    }

    @Override
    public Mono<Order> approveOrder(UUID orderId) {
        return repository.findById(orderId)
                .flatMap(order -> {
                    order.setStatus(OrderStatus.APPROVED);
                    order.setUpdatedAt(LocalDateTime.now());
                    return repository.save(order);
                })
                .doOnSuccess(order -> {
                    log.info("Orden {} aprobada", orderId);
                    eventBus.publish(EventType.ORDER_APPROVED, order);
                });
    }

    @Override
    public Mono<Order> rejectOrder(UUID orderId) {
        return repository.findById(orderId)
                .flatMap(order -> {
                    order.setStatus(OrderStatus.REJECTED);
                    order.setUpdatedAt(LocalDateTime.now());
                    return repository.save(order);
                })
                .doOnSuccess(order -> {
                    log.info("❌ Orden {} rechazada", orderId);
                    eventBus.publish(EventType.ORDER_REJECTED, order);
                });
    }

    @Override
    public Mono<Order> updateOrderStatus(UUID orderId, String status) {
        return repository.findById(orderId)
                .flatMap(order -> {
                    order.setStatus(OrderStatus.valueOf(status));
                    order.setUpdatedAt(LocalDateTime.now());
                    return repository.save(order);
                })
                .doOnSuccess(order -> {
                    log.info("📝 Orden {} estado actualizado a {}", orderId, status);
                    eventBus.publish(EventType.ORDER_STATUS_UPDATED, order);
                });
    }

    @Override
    public Flux<Order> getAllOrders() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Flux<Order> getOrdersByStatus(OrderStatus status) {
        return repository.findByStatusOrderByCreatedAtAsc(status);
    }

    @Override
    public Flux<Order> getOrdersByClientName(String clientName) {
        log.debug("🔍 Consultando historial de órdenes para el cliente: {}", clientName);

        return repository
                .findByClientNameOrderByCreatedAtDesc(clientName)
                .doOnError(e -> log.error(
                        "❌ Error al recuperar órdenes para el cliente {}: {}",
                        clientName,
                        e.getMessage()
                ));
    }
}