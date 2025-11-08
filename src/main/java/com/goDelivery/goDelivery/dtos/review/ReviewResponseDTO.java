package com.goDelivery.goDelivery.dtos.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long reviewId;
    private Long orderId;
    private Long customerId;
    private String customerName; // Will be null if isAnonymous is true
    private Long restaurantId;
    private String restaurantName;
    private Long bikerId;
    private String bikerName; // Only if biker is reviewed
    private Integer foodRating;
    private Integer deliveryRating;
    private Integer overallRating;
    private String reviewText;
    private List<String> reviewImages;
    private boolean isAnonymous;
    private boolean isVerified;
    private int helpfulCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
    
    // Response from restaurant/biker (if any)
    private String responseText;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate responseDate;
}
