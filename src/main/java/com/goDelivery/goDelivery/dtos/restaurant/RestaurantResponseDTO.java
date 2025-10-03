package com.goDelivery.goDelivery.dtos.restaurant;

import com.goDelivery.goDelivery.Enum.RestaurantSetupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponseDTO {
    private Long restaurantId;
    private String restaurantName;
    private String description;
    private String cuisineType;
    private String email;
    private String phoneNumber;
    private String logoUrl;
    private String bannerUrl;
    private String coverImageUrl;
    private String address;
    private String city;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private Double deliveryRadius;
    private RestaurantSetupStatus setupStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
