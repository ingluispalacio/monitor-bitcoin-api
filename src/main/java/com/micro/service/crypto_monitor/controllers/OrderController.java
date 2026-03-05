package com.micro.service.crypto_monitor.controllers;

import com.micro.service.crypto_monitor.business.OrderService;
import com.micro.service.crypto_monitor.dto.OrderRequestDTO;
import com.micro.service.crypto_monitor.enums.OrderStatus;
import com.micro.service.crypto_monitor.model.FeatureToggle;
import com.micro.service.crypto_monitor.model.Order;
import com.micro.service.crypto_monitor.repository.FeatureToggleRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("${application.request.mappings}/orders")
@RequiredArgsConstructor
@Tag(name = "Órdenes", description = "Gestión de órdenes de compra de criptomonedas")
public class OrderController {

    private final OrderService orderService; // Inyectamos el Service, no el Repo directamente
    private final FeatureToggleRepository toggleRepository;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public Mono<Order> createOrder(@RequestBody OrderRequestDTO orderDTO) {
        return toggleRepository.findByModuleName("CRYPTO_MONITOR")
                .map(FeatureToggle::isActive)
                .defaultIfEmpty(false)
                .flatMap(isActive -> {
                    if (!isActive) {
                        return Mono.error(new RuntimeException("El sistema de compras está deshabilitado."));
                    }

                    return ReactiveSecurityContextHolder.getContext()
                            .map(ctx -> ctx.getAuthentication().getName())
                            .flatMap(username -> orderService.createOrder(
                                    username,
                                    orderDTO.getCryptoName(),
                                    orderDTO.getAmount().toString(),
                                    orderDTO.getPrice(), 
                                    orderDTO.getTotal() 
                    ));
                });
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CLIENT')")
    public Flux<Order> getMyOrders() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName()) // 👈 usamos username
                .flatMapMany(username -> orderService.getOrdersByClientName(username));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<Order> getAllPendingOrders() {
        return orderService.getOrdersByStatus(OrderStatus.PENDING);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Order> approveOrder(@PathVariable UUID id) {
        // Usamos el Service para que se dispare el EventBus y el Admin/Cliente se
        // enteren por WS
        return orderService.approveOrder(id);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Order> rejectOrder(@PathVariable UUID id) {
        return orderService.rejectOrder(id);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public Mono<Order> cancelOrder(@PathVariable UUID id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> orderService.updateOrderStatus(id, "CANCELLED")
                        .filter(order -> order.getClientName().equals(username))
                        .switchIfEmpty(Mono.error(new RuntimeException("No autorizado o la orden no existe"))));
    }
}