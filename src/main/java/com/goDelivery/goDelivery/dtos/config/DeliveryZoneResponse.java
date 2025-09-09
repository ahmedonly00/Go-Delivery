package com.goDelivery.goDelivery.dtos.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryZoneResponse {
    private Long id;
    private String name;
    private String description;
    private String zoneType;
    private boolean isActive;
    private Double centerLatitude;
    private Double centerLongitude;
    private Double radiusInKm;
    private List<GeoPoint> polygonCoordinates;
    private List<String> postalCodes;
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime;
    private Double minimumOrderAmount;
    private List<String> supportedDeliveryTypes;
    private List<OperatingHours> operatingHours;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private int totalRestaurants;
    private int totalActiveOrders;
    private int totalDeliveriesCompleted;
    private double averageDeliveryTime;
    private double averageRating;

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
        private String dayOfWeek;
        private boolean is24Hours;
        private String openTime;
        private String closeTime;
        private boolean isClosed;
    }
}
