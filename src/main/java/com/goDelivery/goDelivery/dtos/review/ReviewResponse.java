package com.goDelivery.goDelivery.dtos.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private String reviewId;
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerImage;
    private String restaurantId;
    private String restaurantName;
    private Integer rating;
    private String comment;
    private String tag;
    private boolean isAnonymous;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isEdited;
    private boolean isOwnerResponse;
    private String ownerResponse;
    private LocalDateTime ownerResponseDate;
    private List<ReviewHelpful> helpfulVotes;
    private int helpfulCount;
    private boolean isHelpfulByCurrentUser;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewHelpful {
        private String userId;
        private String userName;
        private LocalDateTime votedAt;
    }
}
