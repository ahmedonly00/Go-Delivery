package com.goDelivery.goDelivery.dtos.review;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
    
    private Long bikerId; // Optional, only if reviewing the biker
    
    @NotNull(message = "Food rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private Integer foodRating;
    
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private Integer deliveryRating; // For biker review
    
    @Size(max = 1000, message = "Review text cannot exceed 1000 characters")
    private String reviewText;
    
    private String reviewImages; // Comma-separated image URLs
    
    private boolean isAnonymous = false;
}
