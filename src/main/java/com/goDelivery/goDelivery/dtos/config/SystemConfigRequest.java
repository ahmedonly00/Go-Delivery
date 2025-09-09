package com.goDelivery.goDelivery.dtos.config;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class SystemConfigRequest {
    @NotBlank(message = "Config key is required")
    private String configKey;
    
    @NotBlank(message = "Config value is required")
    private String configValue;
    
    private String description;
    private boolean isPublic = false;
    private String dataType = "STRING"; // STRING, NUMBER, BOOLEAN, JSON, etc.
    private String groupName;
    private int displayOrder = 0;
    private boolean isRequired = false;
    private String validationRegex;
    private String validationMessage;
    private String[] allowedValues;
    private String dependsOn;
    private String[] dependsOnValues;
}
