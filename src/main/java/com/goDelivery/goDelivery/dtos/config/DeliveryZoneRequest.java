package com.goDelivery.goDelivery.dtos.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DeliveryZoneRequest {
    @NotBlank(message = "Zone name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Zone type is required")
    private String zoneType; // POLYGON, RADIUS, POSTAL_CODES, etc.
    
    @NotNull(message = "Is active status is required")
    private Boolean isActive = true;
    
    // For RADIUS type
    private Double centerLatitude;
    private Double centerLongitude;
    private Double radiusInKm;
    
    // For POLYGON type
    private List<GeoPoint> polygonCoordinates;
    
    // For POSTAL_CODES type
    private List<String> postalCodes;
    
    // Delivery settings
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime; // in minutes
    private Double minimumOrderAmount;
    private List<String> supportedDeliveryTypes; // STANDARD, EXPRESS, SCHEDULED, etc.
    
    // Operating hours
    private List<OperatingHours> operatingHours;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeoPoint {
        private Double latitude;
        private Double longitude;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHours {
        private String dayOfWeek; // MONDAY, TUESDAY, etc.
        private boolean is24Hours;
        private String openTime; // HH:mm format
        private String closeTime; // HH:mm format
        private boolean isClosed;
    }
}
