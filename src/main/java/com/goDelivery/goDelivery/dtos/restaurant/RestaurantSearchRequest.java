package com.goDelivery.goDelivery.dtos.restaurant;

import lombok.Data;

@Data
public class RestaurantSearchRequest {
    private String location;
    private String restaurantName;
    private String cuisineType;
    private boolean hasPromotions;
    private String sortBy; // "rating", "popularity", "deliveryTime", "deliveryFee"
    private String searchQuery;
    private Double minRating;
    private Double maxDeliveryFee;
    private Integer maxDeliveryTime;
}
