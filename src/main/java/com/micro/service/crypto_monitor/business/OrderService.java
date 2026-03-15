package com.micro.service.crypto_monitor.business;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.micro.service.crypto_monitor.dto.ApiResponseDTO;
import com.micro.service.crypto_monitor.enums.OrderStatus;
import com.micro.service.crypto_monitor.model.Order;
import reactor.core.publisher.Mono;

public interface OrderService {

    Mono<ApiResponseDTO<Order>> createOrder(String clientName, String crypto, String amount, BigDecimal price,
            BigDecimal total);

    Mono<ApiResponseDTO<Order>> createOrderWithToggleCheck(String clientName, String crypto, String amount, BigDecimal price,
            BigDecimal total);

    Mono<ApiResponseDTO<Order>> updateOrderStatus(UUID orderId, String status);

    Mono<ApiResponseDTO<Order>> cancelOrder(UUID orderId, String username);

    Mono<ApiResponseDTO<Order>> approveOrder(UUID orderId);  

    Mono<ApiResponseDTO<Order>> rejectOrder(UUID orderId);
    
    Mono<ApiResponseDTO<List<Order>>> getAllOrders();
    
    Mono<ApiResponseDTO<List<Order>>> getOrdersByClientName(String username);
    
    Mono<ApiResponseDTO<List<Order>>> getOrdersByStatus(OrderStatus status);
}