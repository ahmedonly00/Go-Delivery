package com.goDelivery.goDelivery.dtos.review;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private Integer rating;
    
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;
    
    @Size(max = 50, message = "Tag cannot exceed 50 characters")
    private String tag; // e.g., "Food Quality", "Delivery Time", etc.
    
    private Boolean isAnonymous = false;
}
