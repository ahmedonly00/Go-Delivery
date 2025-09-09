package com.goDelivery.goDelivery.dtos.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponse {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private boolean isPublic;
    private String dataType;
    private String groupName;
    private int displayOrder;
    private boolean isRequired;
    private String validationRegex;
    private String validationMessage;
    private String[] allowedValues;
    private String dependsOn;
    private String[] dependsOnValues;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status; // ACTIVE, INACTIVE, DEPRECATED
    private String version;
}
