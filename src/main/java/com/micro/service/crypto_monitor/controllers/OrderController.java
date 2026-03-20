package com.micro.service.crypto_monitor.controllers;

import com.micro.service.crypto_monitor.business.OrderService;
import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.OrderRequestDTO;
import com.micro.service.crypto_monitor.dto.OrderResponseDTO;
import com.micro.service.crypto_monitor.enums.OrderStatus;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${application.request.mappings}/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Órdenes", description = "Gestión de órdenes de compra de criptomonedas")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public Mono<ApiResponseDTO<OrderResponseDTO>> createOrder(@RequestBody OrderRequestDTO orderDTO) {
        log.info("Inicio controller createOrder con request -> {}", orderDTO);
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> orderService.createOrderWithToggleCheck( orderDTO ));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CLIENT')")
    public Mono<ApiResponseDTO<List<OrderResponseDTO>>> getMyOrders() {
        log.info("Inicio controller getMyOrders");
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> orderService.getOrdersByClientName(username));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<List<OrderResponseDTO>>> getAllPendingOrders() {
        log.info("Inicio controller getAllPendingOrders");
        return orderService.getOrdersByStatus(OrderStatus.PENDING);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<List<OrderResponseDTO>>> getAllOrders() {
        log.info("Inicio controller getAllOrders");
        return orderService.getAllOrders();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<OrderResponseDTO>> approveOrder(@PathVariable UUID id) {
        log.info("Inicio controller approveOrder, id={}", id);
        return orderService.approveOrder(id);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ApiResponseDTO<OrderResponseDTO>> rejectOrder(@PathVariable UUID id) {
        log.info("Inicio controller rejectOrder, id={}", id);
        return orderService.rejectOrder(id);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public Mono<ApiResponseDTO<OrderResponseDTO>> cancelOrder(@PathVariable UUID id) {
        log.info("Inicio controller cancelOrder, id={}", id);
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> orderService.updateOrderStatus(id, "CANCELLED")
                        .filter(response -> response.getData() != null && response.getData().getClientName().equals(username))
                        .switchIfEmpty(Mono.error(new RuntimeException("No autorizado o la orden no existe"))));
    }
}