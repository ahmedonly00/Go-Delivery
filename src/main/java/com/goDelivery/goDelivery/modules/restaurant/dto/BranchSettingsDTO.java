package com.goDelivery.goDelivery.modules.restaurant.dto;

import com.goDelivery.goDelivery.shared.enums.DeliveryType;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchSettingsDTO {

    private DeliveryType deliveryType;

    @DecimalMin(value = "0.0", message = "Delivery fee cannot be negative")
    private BigDecimal deliveryFee;

    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    private BigDecimal minimumOrderAmount;

    @DecimalMin(value = "1", message = "Preparation time must be at least 1 minute")
    private Integer averagePreparationTime; // in minutes

    private Boolean deliveryAvailable;

    private Double deliveryRadius;

    private Boolean isActive;

    private OperatingHoursDTO operatingHours;
}
