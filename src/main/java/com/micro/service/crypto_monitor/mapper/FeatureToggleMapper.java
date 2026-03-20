package com.micro.service.crypto_monitor.mapper;

import com.micro.service.crypto_monitor.dto.FeatureToggleDTO;
import com.micro.service.crypto_monitor.model.FeatureToggle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FeatureToggleMapper {
    FeatureToggleDTO toDTO(FeatureToggle entity);

    FeatureToggle toEntity(FeatureToggleDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(FeatureToggleDTO dto, @org.mapstruct.MappingTarget FeatureToggle entity);
}