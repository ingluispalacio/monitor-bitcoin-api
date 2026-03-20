package com.micro.service.crypto_monitor.mapper;

import com.micro.service.crypto_monitor.dto.OrderRequestDTO;
import com.micro.service.crypto_monitor.dto.OrderResponseDTO;
import com.micro.service.crypto_monitor.enums.OrderStatus;
import com.micro.service.crypto_monitor.model.Order;
import org.mapstruct.*;

import java.time.LocalDateTime;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    imports = {LocalDateTime.class, OrderStatus.class}
)
public interface OrderMapper {

    OrderResponseDTO toDTO(Order entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "total", expression = "java(dto.getAmount().multiply(dto.getPrice()))")
    @Mapping(target = "status", expression = "java(OrderStatus.PENDING)")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    Order toEntity(OrderRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "total", expression = "java(dto.getAmount() != null && dto.getPrice() != null ? dto.getAmount().multiply(dto.getPrice()) : entity.getTotal())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    void updateEntityFromDTO(OrderRequestDTO dto, @MappingTarget Order entity);
}