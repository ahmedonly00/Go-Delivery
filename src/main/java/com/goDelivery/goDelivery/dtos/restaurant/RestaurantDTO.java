package com.goDelivery.goDelivery.dtos.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {
    private Long restaurantId;
    private String restaurantName;
    private String location;
    private String cuisineType;
    private String email;
    private String phoneNumber;
    private String logo;
    private Float rating;
    private Integer totalReviews;
    private Integer totalOrders;
    private Integer averagePreparationTime;
    private Float deliveryFee;
    private Float minimumOrderAmount;
    private boolean isActive;
    private List<BranchDTO> branches;
}
