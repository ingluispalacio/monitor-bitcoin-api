package com.micro.service.crypto_monitor.dto;

import lombok.Data;

@Data
public class FeatureToggleDTO {

    private String moduleName;
    private Boolean active;
}
