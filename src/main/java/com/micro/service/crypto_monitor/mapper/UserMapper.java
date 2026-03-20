package com.micro.service.crypto_monitor.mapper;

import com.micro.service.crypto_monitor.dto.UserRequestDTO;
import com.micro.service.crypto_monitor.dto.UserResponseDTO;
import com.micro.service.crypto_monitor.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {
    UserResponseDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true) 
    User toEntity(UserRequestDTO dto);
}