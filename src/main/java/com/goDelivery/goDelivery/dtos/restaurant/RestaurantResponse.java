package com.goDelivery.goDelivery.dtos.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long restaurantId;
    private String restaurantName;
    private String location;
    private String cuisineType;
    private String logoUrl;
    private Float rating;
    private Integer totalReviews;
    private Integer totalOrders;
    private Integer averagePreparationTime;
    private Float deliveryFee;
    private Float minimumOrderAmount;
    private boolean isOpen;
    private boolean hasPromotions;
}
