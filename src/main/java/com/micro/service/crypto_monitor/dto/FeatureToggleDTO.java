package com.micro.service.crypto_monitor.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class FeatureToggleDTO {
    private UUID id;
    private String moduleName;
    private Boolean active;
}
