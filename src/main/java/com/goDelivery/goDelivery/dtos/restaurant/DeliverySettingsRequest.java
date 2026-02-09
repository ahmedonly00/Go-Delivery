package com.goDelivery.goDelivery.dtos.restaurant;

import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.Enum.DistanceUnit;
import com.goDelivery.goDelivery.validation.ValidDeliverySettings;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@ValidDeliverySettings
public class DeliverySettingsRequest {
    @NotNull(message = "Delivery type is required")
    private DeliveryType deliveryType;

    // Only for SELF_DELIVERY
    private DistanceUnit radiusUnit;

    @Positive(message = "Base delivery fee must be a positive number")
    private Float baseDeliveryFee; // Base fee for SELF_DELIVERY

    @Positive(message = "Per kilometer fee must be a positive number")
    private Float perKmFee; // Additional fee per km for SELF_DELIVERY

    @Positive(message = "Delivery radius must be a positive number")
    private Double deliveryRadius; // Radius value, unit specified by radiusUnit

    // Only for SYSTEM_DELIVERY
    private Boolean acceptSystemDeliveryAgreement;
}
