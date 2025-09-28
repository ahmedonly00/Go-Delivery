package com.goDelivery.goDelivery.dtos.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Food rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer foodRating;

    @NotNull(message = "Delivery rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer deliveryRating;

    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer overallRating;

    @Size(max = 1000, message = "Food review must be less than 1000 characters")
    private String foodReview;

    @Size(max = 1000, message = "Delivery review must be less than 1000 characters")
    private String deliveryReview;
}
