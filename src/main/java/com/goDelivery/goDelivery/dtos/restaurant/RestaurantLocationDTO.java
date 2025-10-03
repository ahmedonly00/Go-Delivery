package com.goDelivery.goDelivery.dtos.restaurant;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RestaurantLocationDTO {
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotBlank(message = "City is required")
    private String city;
    
    private String postalCode;
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Digits(integer = 2, fraction = 6, message = "Invalid latitude format")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Digits(integer = 3, fraction = 6, message = "Invalid longitude format")
    private Double longitude;
    
    @NotNull(message = "Delivery radius is required")
    @DecimalMin(value = "0.5", message = "Minimum delivery radius is 0.5 km")
    @DecimalMax(value = "50.0", message = "Maximum delivery radius is 50 km")
    private Double deliveryRadius = 5.0;
}
