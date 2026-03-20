package com.micro.service.crypto_monitor.business;

import java.util.List;
import java.util.UUID;

import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.dto.OrderRequestDTO;
import com.micro.service.crypto_monitor.dto.OrderResponseDTO;
import com.micro.service.crypto_monitor.enums.OrderStatus;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<ApiResponseDTO<OrderResponseDTO>> createOrder(OrderRequestDTO orderDTO);

    Mono<ApiResponseDTO<OrderResponseDTO>> createOrderWithToggleCheck(OrderRequestDTO orderDTO);

    Mono<ApiResponseDTO<OrderResponseDTO>> updateOrderStatus(UUID orderId, String status);

    Mono<ApiResponseDTO<OrderResponseDTO>> cancelOrder(UUID orderId, String username);

    Mono<ApiResponseDTO<OrderResponseDTO>> approveOrder(UUID orderId);  

    Mono<ApiResponseDTO<OrderResponseDTO>> rejectOrder(UUID orderId);
    
    Mono<ApiResponseDTO<List<OrderResponseDTO>>> getAllOrders();
    
    Mono<ApiResponseDTO<List<OrderResponseDTO>>> getOrdersByClientName(String username);
    
    Mono<ApiResponseDTO<List<OrderResponseDTO>>> getOrdersByStatus(OrderStatus status);
}