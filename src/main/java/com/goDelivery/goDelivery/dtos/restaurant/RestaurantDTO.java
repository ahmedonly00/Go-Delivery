package com.goDelivery.goDelivery.dtos.restaurant;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {
    private Long restaurantId;
    
    @NotBlank(message = "Restaurant name is required")
    private String restaurantName;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotBlank(message = "Cuisine type is required")
    private String cuisineType;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number should be valid")
    private String phoneNumber;
    
    @NotBlank(message = "Logo URL is required")
    private String logoUrl;

    @NotBlank(message = "Description is required")
    private String description;
    
    @Builder.Default
    private Double rating = 0.0;
    
    @Builder.Default
    private Integer totalOrders = 0;
    
    @Builder.Default
    private Integer totalReviews = 0;
    
    @PositiveOrZero(message = "Average preparation time must be zero or positive")
    private Integer averagePreparationTime;
    
    @NotNull(message = "Delivery fee is required")
    @PositiveOrZero(message = "Delivery fee must be zero or positive")
    private Float deliveryFee;
    
    @NotNull(message = "Minimum order amount is required")
    @PositiveOrZero(message = "Minimum order amount must be zero or positive")
    private Float minimumOrderAmount;

    private OperatingHoursDTO operatingHours;
    
    @Builder.Default
    private boolean isActive = true;

    private LocalDate createdAt;
    private LocalDate updatedAt;
    
}
