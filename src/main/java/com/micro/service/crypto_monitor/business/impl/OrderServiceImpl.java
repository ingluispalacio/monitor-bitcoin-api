package com.micro.service.crypto_monitor.business.impl;

import com.micro.service.crypto_monitor.business.OrderService;
import com.micro.service.crypto_monitor.business.FeatureToggleService;
import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.enums.EventType;
import com.micro.service.crypto_monitor.enums.OrderStatus;
import com.micro.service.crypto_monitor.model.Order;
import com.micro.service.crypto_monitor.repository.OrderRepository;
import com.micro.service.crypto_monitor.websocket.EventBus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final EventBus eventBus;
    private final FeatureToggleService featureToggleService;

    @Override
    public Mono<ApiResponseDTO<Order>> createOrder(String clientName, String crypto, String amount, BigDecimal price,
            BigDecimal total) {

        log.info("SERVICIO ORDEN - CREAR - Iniciando creación de orden para cliente={} crypto={}", clientName, crypto);

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
                    log.info("SERVICIO ORDEN - CREAR - Orden creada id={} cliente={} precio=${}",
                            savedOrder.getId(), clientName, price);

                    log.debug("SERVICIO ORDEN - EVENTO - Publicando evento ORDER_CREATED id={}", savedOrder.getId());
                    eventBus.publish(EventType.ORDER_CREATED, savedOrder);
                })
                .doOnError(e -> log.error("SERVICIO ORDEN - CREAR - Error al guardar orden cliente={} error={}",
                        clientName, e.getMessage(), e))
                .map(savedOrder -> ApiResponseDTO.<Order>builder()
                        .success(true)
                        .message("Orden creada correctamente")
                        .data(savedOrder)
                        .timestamp(LocalDateTime.now())
                        .build())
                .onErrorResume(ex -> Mono.just(ApiResponseDTO.<Order>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()));
    }

    @Override
    public Mono<ApiResponseDTO<Order>> createOrderWithToggleCheck(String clientName, String crypto, String amount,
            BigDecimal price, BigDecimal total) {

        log.info("SERVICIO ORDEN - CREAR CON TOGGLE - Verificando feature toggle para cliente={}", clientName);

        return featureToggleService.isActive("CRYPTO_MONITOR")
                .flatMap(response -> {

                    log.debug("SERVICIO ORDEN - TOGGLE - Estado del toggle CRYPTO_MONITOR={}", response.getData());

                    if (!response.getData()) {
                        log.warn("SERVICIO ORDEN - TOGGLE - Sistema de compras deshabilitado");
                        return Mono.error(new RuntimeException("El sistema de compras está deshabilitado."));
                    }

                    return createOrder(clientName, crypto, amount, price, total);
                })
                .doOnNext(resp -> log.info(
                        "SERVICIO ORDEN - CREAR CON TOGGLE - Resultado cliente={} success={} mensaje={}",
                        clientName, resp.isSuccess(), resp.getMessage()))
                .onErrorResume(ex -> {

                    log.error("SERVICIO ORDEN - CREAR CON TOGGLE - Error cliente={} error={}",
                            clientName, ex.getMessage(), ex);

                    return Mono.just(ApiResponseDTO.<Order>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<Order>> approveOrder(UUID orderId) {

        log.info("SERVICIO ORDEN - APROBAR - Iniciando aprobación de orden id={}", orderId);

        return repository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Orden no encontrada")))
                .flatMap(order -> {
                    order.setStatus(OrderStatus.APPROVED);
                    order.setUpdatedAt(LocalDateTime.now());
                    return repository.save(order);
                })
                .doOnSuccess(order -> {

                    log.info("SERVICIO ORDEN - APROBAR - Orden aprobada id={}", orderId);

                    log.debug("SERVICIO ORDEN - EVENTO - Publicando evento ORDER_APPROVED id={}", orderId);
                    eventBus.publish(EventType.ORDER_APPROVED, order);
                })
                .map(order -> ApiResponseDTO.<Order>builder()
                        .success(true)
                        .message("Orden aprobada correctamente")
                        .data(order)
                        .timestamp(LocalDateTime.now())
                        .build())
                .onErrorResume(ex -> {

                    log.error("SERVICIO ORDEN - APROBAR - Error id={} error={}", orderId, ex.getMessage(), ex);

                    return Mono.just(ApiResponseDTO.<Order>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<Order>> rejectOrder(UUID orderId) {

        log.info("SERVICIO ORDEN - RECHAZAR - Iniciando rechazo de orden id={}", orderId);

        return repository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Orden no encontrada")))
                .flatMap(order -> {
                    order.setStatus(OrderStatus.REJECTED);
                    order.setUpdatedAt(LocalDateTime.now());
                    return repository.save(order);
                })
                .doOnSuccess(order -> {

                    log.info("SERVICIO ORDEN - RECHAZAR - Orden rechazada id={}", orderId);

                    log.debug("SERVICIO ORDEN - EVENTO - Publicando evento ORDER_REJECTED id={}", orderId);
                    eventBus.publish(EventType.ORDER_REJECTED, order);
                })
                .map(order -> ApiResponseDTO.<Order>builder()
                        .success(true)
                        .message("Orden rechazada correctamente")
                        .data(order)
                        .timestamp(LocalDateTime.now())
                        .build())
                .onErrorResume(ex -> {

                    log.error("SERVICIO ORDEN - RECHAZAR - Error id={} error={}", orderId, ex.getMessage(), ex);

                    return Mono.just(ApiResponseDTO.<Order>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<Order>> updateOrderStatus(UUID orderId, String status) {
        return repository.findById(orderId)
                .flatMap(order -> {
                    order.setStatus(OrderStatus.valueOf(status));
                    order.setUpdatedAt(LocalDateTime.now());
                    return repository.save(order);
                })
                .doOnSuccess(order -> {
                    log.info("📝 Orden {} estado actualizado a {}", orderId, status);
                    eventBus.publish(EventType.ORDER_STATUS_UPDATED, order);
                })
                .map(order -> ApiResponseDTO.<Order>builder()
                        .success(true)
                        .message("Estado de orden actualizado correctamente")
                        .data(order)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("updateOrderStatus success id={} status={}", orderId, status))
                .onErrorResume(ex -> {
                    log.error("updateOrderStatus error id={}  status={} - {}", orderId, status, ex.getMessage());
                    return Mono.just(ApiResponseDTO.<Order>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<Order>> cancelOrder(UUID orderId, String username) {

        log.info("SERVICIO ORDEN - CANCELAR - Solicitud cancelación id={} usuario={}", orderId, username);

        return repository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Orden no encontrada")))
                .flatMap(order -> {

                    if (!order.getClientName().equals(username)) {
                        log.warn("SERVICIO ORDEN - CANCELAR - Usuario no autorizado id={} usuario={}", orderId,
                                username);
                        return Mono.error(new RuntimeException("No autorizado o la orden no existe"));
                    }

                    order.setStatus(OrderStatus.CANCELLED);
                    order.setUpdatedAt(LocalDateTime.now());
                    return repository.save(order);
                })
                .doOnSuccess(order -> {

                    log.info("SERVICIO ORDEN - CANCELAR - Orden cancelada id={} usuario={}", orderId, username);

                    eventBus.publish(EventType.ORDER_STATUS_UPDATED, order);
                })
                .map(order -> ApiResponseDTO.<Order>builder()
                        .success(true)
                        .message("Orden cancelada correctamente")
                        .data(order)
                        .timestamp(LocalDateTime.now())
                        .build())
                .onErrorResume(ex -> {

                    log.error("SERVICIO ORDEN - CANCELAR - Error id={} usuario={} error={}",
                            orderId, username, ex.getMessage(), ex);

                    return Mono.just(ApiResponseDTO.<Order>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<List<Order>>> getAllOrders() {
        return repository.findAllByOrderByCreatedAtDesc()
                .collectList()
                .map(list -> ApiResponseDTO.<List<Order>>builder()
                        .success(true)
                        .message("Todas las órdenes obtenidas correctamente")
                        .data(list)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("getAllOrders success count={}", resp.getData().size()))
                .onErrorResume(ex -> {
                    log.error("getAllOrders error: {}", ex.getMessage());
                    return Mono.just(ApiResponseDTO.<List<Order>>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<List<Order>>> getOrdersByStatus(OrderStatus status) {
        return repository.findByStatusOrderByCreatedAtAsc(status)
                .collectList()
                .map(list -> ApiResponseDTO.<List<Order>>builder()
                        .success(true)
                        .message("Órdenes por estado obtenidas correctamente")
                        .data(list)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(
                        resp -> log.info("getOrdersByStatus success status={} count={}", status, resp.getData().size()))
                .onErrorResume(ex -> {
                    log.error("getOrdersByStatus error status={} - {}", status, ex.getMessage());
                    return Mono.just(ApiResponseDTO.<List<Order>>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    @Override
    public Mono<ApiResponseDTO<List<Order>>> getOrdersByClientName(String clientName) {
        log.info("Inicio service getOrdersByClientName clientName={}", clientName);

        return repository
                .findByClientNameOrderByCreatedAtDesc(clientName)
                .collectList()
                .map(list -> ApiResponseDTO.<List<Order>>builder()
                        .success(true)
                        .message("Órdenes del cliente obtenidas correctamente")
                        .data(list)
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnNext(resp -> log.info("getOrdersByClientName success clientName={} count={}", clientName,
                        resp.getData().size()))
                .onErrorResume(ex -> {
                    log.error("getOrdersByClientName error clientName={} - {}", clientName, ex.getMessage());
                    return Mono.just(ApiResponseDTO.<List<Order>>builder()
                            .success(false)
                            .message(ex.getMessage())
                            .data(null)
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }
}