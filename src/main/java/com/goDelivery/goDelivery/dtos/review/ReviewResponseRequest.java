package com.goDelivery.goDelivery.dtos.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewResponseRequest {
    @NotBlank(message = "Review ID is required")
    private String reviewId;
    
    @NotBlank(message = "Response cannot be empty")
    @Size(max = 1000, message = "Response cannot exceed 1000 characters")
    private String response;
}
