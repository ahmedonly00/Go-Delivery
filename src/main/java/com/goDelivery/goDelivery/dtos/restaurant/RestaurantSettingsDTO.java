package com.goDelivery.goDelivery.dtos.restaurant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RestaurantSettingsDTO {
    
    @NotNull(message = "Delivery fee is required")
    @DecimalMin(value = "0.0", message = "Delivery fee cannot be negative")
    private BigDecimal deliveryFee;
    
    @NotNull(message = "Minimum order amount is required")
    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    private BigDecimal minimumOrderAmount;
    
    @NotNull(message = "Average preparation time is required")
    @DecimalMin(value = "1", message = "Preparation time must be at least 1 minute")
    private Integer averagePreparationTime; // in minutes
    
    @NotNull(message = "Operating hours are required")
    private OperatingHoursDTO operatingHours;
    
    private boolean isActive = true;
}
