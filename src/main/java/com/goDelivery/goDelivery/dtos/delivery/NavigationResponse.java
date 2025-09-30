package com.goDelivery.goDelivery.dtos.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NavigationResponse {
    
    private Long orderId;
    private String orderNumber;
    private String destinationType; // RESTAURANT or CUSTOMER
    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double distanceKm;
    private Integer estimatedTimeMinutes;
    private String navigationUrl;
    private String googleMapsUrl;
    private String wazeUrl;
    private RouteInfo routeInfo;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RouteInfo {
        private Double totalDistanceKm;
        private Integer totalTimeMinutes;
        private String summary;
        private String warnings;
    }
}
