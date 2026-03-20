package com.micro.service.crypto_monitor.business.impl;

import com.micro.service.crypto_monitor.business.OrderService;
import com.micro.service.crypto_monitor.business.FeatureToggleService;
import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.OrderRequestDTO;
import com.micro.service.crypto_monitor.dto.OrderResponseDTO;
import com.micro.service.crypto_monitor.enums.EventType;
import com.micro.service.crypto_monitor.enums.OrderStatus;
import com.micro.service.crypto_monitor.mapper.OrderMapper;
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
        private final OrderMapper orderMapper;

        @Override
        public Mono<ApiResponseDTO<OrderResponseDTO>> createOrder(OrderRequestDTO dto) {

                log.info("SERVICIO ORDEN - CREAR - cliente={} crypto={}",
                                dto.getClientName(), dto.getCryptoName());

                // 🔥 convertir DTO → Entity (ya calcula total, status y fechas en el mapper)
                Order order = orderMapper.toEntity(dto);

                return repository.save(order)
                                .doOnSuccess(savedOrder -> {
                                        log.info("ORDEN CREADA id={} cliente={} total={}",
                                                        savedOrder.getId(),
                                                        savedOrder.getClientName(),
                                                        savedOrder.getTotal());

                                        // 🔥 evento
                                        eventBus.publish(EventType.ORDER_CREATED, savedOrder);
                                })
                                .map(savedOrder -> ApiResponseDTO.<OrderResponseDTO>builder()
                                                .success(true)
                                                .message("Orden creada correctamente")
                                                .data(orderMapper.toDTO(savedOrder)) // ✅ Entity → DTO
                                                .timestamp(LocalDateTime.now())
                                                .build())
                                .onErrorResume(ex -> {
                                        log.error("ERROR CREANDO ORDEN cliente={} error={}",
                                                        dto.getClientName(), ex.getMessage(), ex);

                                        return Mono.just(ApiResponseDTO.<OrderResponseDTO>builder()
                                                        .success(false)
                                                        .message(ex.getMessage())
                                                        .data(null)
                                                        .timestamp(LocalDateTime.now())
                                                        .build());
                                });
        }

        @Override
        public Mono<ApiResponseDTO<OrderResponseDTO>> createOrderWithToggleCheck(OrderRequestDTO dto) {

                log.info("SERVICIO ORDEN - CREAR CON TOGGLE - cliente={} crypto={}",
                                dto.getClientName(), dto.getCryptoName());

                return featureToggleService.isActive("CRYPTO_MONITOR")
                                .flatMap(response -> {

                                        log.debug("SERVICIO ORDEN - TOGGLE - CRYPTO_MONITOR={}", response.getData());

                                        if (!Boolean.TRUE.equals(response.getData())) {
                                                log.warn("SERVICIO ORDEN - TOGGLE - Sistema de compras deshabilitado");
                                                return Mono.error(new RuntimeException(
                                                                "El sistema de compras está deshabilitado."));
                                        }

                                        return createOrder(dto);
                                })
                                .doOnNext(resp -> log.info(
                                                "SERVICIO ORDEN - CREAR CON TOGGLE - cliente={} success={} mensaje={}",
                                                dto.getClientName(), resp.isSuccess(), resp.getMessage()))
                                .onErrorResume(ex -> {

                                        log.error("SERVICIO ORDEN - CREAR CON TOGGLE - cliente={} error={}",
                                                        dto.getClientName(), ex.getMessage(), ex);

                                        return Mono.just(ApiResponseDTO.<OrderResponseDTO>builder()
                                                        .success(false)
                                                        .message(ex.getMessage())
                                                        .data(null)
                                                        .timestamp(LocalDateTime.now())
                                                        .build());
                                });
        }

        @Override
        public Mono<ApiResponseDTO<OrderResponseDTO>> approveOrder(UUID orderId) {

                log.info("SERVICIO ORDEN - APROBAR - id={}", orderId);

                return repository.findById(orderId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Orden no encontrada")))
                                .flatMap(order -> {
                                        order.setStatus(OrderStatus.APPROVED);
                                        order.setUpdatedAt(LocalDateTime.now());
                                        return repository.save(order);
                                })
                                .doOnSuccess(savedOrder -> {
                                        log.info("SERVICIO ORDEN - APROBAR - Orden aprobada id={}", orderId);
                                        eventBus.publish(EventType.ORDER_APPROVED, savedOrder);
                                })
                                .map(orderMapper::toDTO)
                                .map(dto -> ApiResponseDTO.<OrderResponseDTO>builder()
                                                .success(true)
                                                .message("Orden aprobada correctamente")
                                                .data(dto)
                                                .timestamp(LocalDateTime.now())
                                                .build())
                                .onErrorResume(ex -> {
                                        log.error("SERVICIO ORDEN - APROBAR - Error id={} error={}", orderId,
                                                        ex.getMessage(), ex);

                                        return Mono.just(ApiResponseDTO.<OrderResponseDTO>builder()
                                                        .success(false)
                                                        .message(ex.getMessage())
                                                        .data(null)
                                                        .timestamp(LocalDateTime.now())
                                                        .build());
                                });
        }

        @Override
        public Mono<ApiResponseDTO<OrderResponseDTO>> rejectOrder(UUID orderId) {

                log.info("SERVICIO ORDEN - RECHAZAR - Iniciando rechazo de orden id={}", orderId);

                return repository.findById(orderId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Orden no encontrada")))
                                .flatMap(order -> {
                                        order.setStatus(OrderStatus.REJECTED);
                                        order.setUpdatedAt(LocalDateTime.now());
                                        return repository.save(order);
                                })
                                .map(orderMapper::toDTO)
                                .doOnSuccess(order -> {

                                        log.info("SERVICIO ORDEN - RECHAZAR - Orden rechazada id={}", orderId);

                                        log.debug("SERVICIO ORDEN - EVENTO - Publicando evento ORDER_REJECTED id={}",
                                                        orderId);
                                        eventBus.publish(EventType.ORDER_REJECTED, order);
                                })
                                .map(order -> ApiResponseDTO.<OrderResponseDTO>builder()
                                                .success(true)
                                                .message("Orden rechazada correctamente")
                                                .data(order)
                                                .timestamp(LocalDateTime.now())
                                                .build())
                                .onErrorResume(ex -> {

                                        log.error("SERVICIO ORDEN - RECHAZAR - Error id={} error={}", orderId,
                                                        ex.getMessage(), ex);

                                        return Mono.just(ApiResponseDTO.<OrderResponseDTO>builder()
                                                        .success(false)
                                                        .message(ex.getMessage())
                                                        .data(null)
                                                        .timestamp(LocalDateTime.now())
                                                        .build());
                                });
        }

        @Override
        public Mono<ApiResponseDTO<OrderResponseDTO>> updateOrderStatus(UUID orderId, String status) {

                log.info("SERVICIO ORDEN - UPDATE STATUS - id={} status={}", orderId, status);

                return repository.findById(orderId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Orden no encontrada")))
                                .flatMap(order -> {

                                        OrderStatus newStatus;
                                        try {
                                                newStatus = OrderStatus.valueOf(status.toUpperCase());
                                        } catch (IllegalArgumentException ex) {
                                                return Mono.error(new RuntimeException("Estado inválido: " + status));
                                        }

                                        order.setStatus(newStatus);
                                        order.setUpdatedAt(LocalDateTime.now());

                                        return repository.save(order);
                                })
                                .doOnSuccess(savedOrder -> {
                                        log.info("📝 Orden {} estado actualizado a {}", orderId, status);
                                        eventBus.publish(EventType.ORDER_STATUS_UPDATED, savedOrder);
                                })
                                .map(orderMapper::toDTO)
                                .map(dto -> ApiResponseDTO.<OrderResponseDTO>builder()
                                                .success(true)
                                                .message("Estado de orden actualizado correctamente")
                                                .data(dto)
                                                .timestamp(LocalDateTime.now())
                                                .build())
                                .doOnNext(resp -> log.info("updateOrderStatus success id={} status={}", orderId,
                                                status))
                                .onErrorResume(ex -> {
                                        log.error("updateOrderStatus error id={} status={} - {}", orderId, status,
                                                        ex.getMessage());

                                        return Mono.just(ApiResponseDTO.<OrderResponseDTO>builder()
                                                        .success(false)
                                                        .message(ex.getMessage())
                                                        .data(null)
                                                        .timestamp(LocalDateTime.now())
                                                        .build());
                                });
        }

        @Override
        public Mono<ApiResponseDTO<OrderResponseDTO>> cancelOrder(UUID orderId, String username) {

                log.info("SERVICIO ORDEN - CANCELAR - Solicitud cancelación id={} usuario={}", orderId, username);

                return repository.findById(orderId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Orden no encontrada")))
                                .flatMap(order -> {

                                        if (!order.getClientName().equals(username)) {
                                                log.warn("SERVICIO ORDEN - CANCELAR - Usuario no autorizado id={} usuario={}",
                                                                orderId,
                                                                username);
                                                return Mono.error(new RuntimeException(
                                                                "No autorizado o la orden no existe"));
                                        }

                                        order.setStatus(OrderStatus.CANCELLED);
                                        order.setUpdatedAt(LocalDateTime.now());
                                        return repository.save(order);
                                })
                                .map(orderMapper::toDTO)
                                .doOnSuccess(order -> {

                                        log.info("SERVICIO ORDEN - CANCELAR - Orden cancelada id={} usuario={}",
                                                        orderId, username);

                                        eventBus.publish(EventType.ORDER_STATUS_UPDATED, order);
                                })
                                .map(order -> ApiResponseDTO.<OrderResponseDTO>builder()
                                                .success(true)
                                                .message("Orden cancelada correctamente")
                                                .data(order)
                                                .timestamp(LocalDateTime.now())
                                                .build())
                                .onErrorResume(ex -> {

                                        log.error("SERVICIO ORDEN - CANCELAR - Error id={} usuario={} error={}",
                                                        orderId, username, ex.getMessage(), ex);

                                        return Mono.just(ApiResponseDTO.<OrderResponseDTO>builder()
                                                        .success(false)
                                                        .message(ex.getMessage())
                                                        .data(null)
                                                        .timestamp(LocalDateTime.now())
                                                        .build());
                                });
        }

        @Override
        public Mono<ApiResponseDTO<List<OrderResponseDTO>>> getAllOrders() {
                return repository.findAllByOrderByCreatedAtDesc()
                                .map(orderMapper::toDTO)
                                .collectList()
                                .map(list -> ApiResponseDTO.<List<OrderResponseDTO>>builder()
                                                .success(true)
                                                .message("Todas las órdenes obtenidas correctamente")
                                                .data(list)
                                                .timestamp(LocalDateTime.now())
                                                .build())
                                .doOnNext(resp -> log.info("getAllOrders success count={}", resp.getData().size()))
                                .onErrorResume(ex -> {
                                        log.error("getAllOrders error: {}", ex.getMessage());
                                        return Mono.just(ApiResponseDTO.<List<OrderResponseDTO>>builder()
                                                        .success(false)
                                                        .message(ex.getMessage())
                                                        .data(null)
                                                        .timestamp(LocalDateTime.now())
                                                        .build());
                                });
        }

        @Override
        public Mono<ApiResponseDTO<List<OrderResponseDTO>>> getOrdersByStatus(OrderStatus status) {
                return repository.findByStatusOrderByCreatedAtAsc(status)
                                .map(orderMapper::toDTO)
                                .collectList()
                                .map(list -> ApiResponseDTO.<List<OrderResponseDTO>>builder()
                                                .success(true)
                                                .message("Órdenes por estado obtenidas correctamente")
                                                .data(list)
                                                .timestamp(LocalDateTime.now())
                                                .build())
                                .doOnNext(
                                                resp -> log.info("getOrdersByStatus success status={} count={}", status,
                                                                resp.getData().size()))
                                .onErrorResume(ex -> {
                                        log.error("getOrdersByStatus error status={} - {}", status, ex.getMessage());
                                        return Mono.just(ApiResponseDTO.<List<OrderResponseDTO>>builder()
                                                        .success(false)
                                                        .message(ex.getMessage())
                                                        .data(null)
                                                        .timestamp(LocalDateTime.now())
                                                        .build());
                                });
        }

        @Override
        public Mono<ApiResponseDTO<List<OrderResponseDTO>>> getOrdersByClientName(String clientName) {
                log.info("Inicio service getOrdersByClientName clientName={}", clientName);

                return repository
                                .findByClientNameOrderByCreatedAtDesc(clientName)
                                .map(orderMapper::toDTO)
                                .collectList()
                                .map(list -> ApiResponseDTO.<List<OrderResponseDTO>>builder()
                                                .success(true)
                                                .message("Órdenes del cliente obtenidas correctamente")
                                                .data(list)
                                                .timestamp(LocalDateTime.now())
                                                .build())
                                .doOnNext(resp -> log.info("getOrdersByClientName success clientName={} count={}",
                                                clientName,
                                                resp.getData().size()))
                                .onErrorResume(ex -> {
                                        log.error("getOrdersByClientName error clientName={} - {}", clientName,
                                                        ex.getMessage());
                                        return Mono.just(ApiResponseDTO.<List<OrderResponseDTO>>builder()
                                                        .success(false)
                                                        .message(ex.getMessage())
                                                        .data(null)
                                                        .timestamp(LocalDateTime.now())
                                                        .build());
                                });
        }
}