package com.goDelivery.goDelivery.dtos.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BikerLocationUpdate {
    @NotNull(message = "Latitude is required")
    private Float currentLatitude;
    
    @NotNull(message = "Longitude is required")
    private Float currentLongitude;
    
    @NotNull(message = "Online status is required")
    private Boolean isOnline;
    
    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;
}
