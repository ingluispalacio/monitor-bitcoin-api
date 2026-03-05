package com.micro.service.crypto_monitor.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("feature_toggle")
public class FeatureToggle {
     @Id
    private UUID id;
    @Column("module_name")
    private String moduleName;
    private boolean active;

}
