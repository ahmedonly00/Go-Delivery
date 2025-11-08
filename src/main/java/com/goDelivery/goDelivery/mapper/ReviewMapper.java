package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.review.ReviewResponseDTO;
import com.goDelivery.goDelivery.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    public ReviewResponseDTO toReviewResponseDTO(Review review) {
        if (review == null) {
            return null;
        }

        List<String> reviewImages = review.getReviewImages() != null ?
                Arrays.stream(review.getReviewImages().split(","))
                        .filter(s -> !s.trim().isEmpty())
                        .collect(Collectors.toList()) :
                List.of();

        // Get the latest response if any
        String responseText = null;
        LocalDate responseDate = null;
        if (review.getResponses() != null && !review.getResponses().isEmpty()) {
            ReviewResponse latestResponse = review.getResponses().get(review.getResponses().size() - 1);
            responseText = latestResponse.getResponseText();
            responseDate = latestResponse.getCreatedAt();
        }

        return ReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .orderId(review.getOrder().getOrderId())
                .customerId(review.getCustomer().getCustomerId())
                .customerName(review.isAnonymous() ? "Anonymous" : 
                    review.getCustomer().getFullName())
                .restaurantId(review.getRestaurant().getRestaurantId())
                .restaurantName(review.getRestaurant().getRestaurantName())
                .bikerId(review.getBikers() != null ? review.getBikers().getBikerId() : null)
                .bikerName(review.getBikers() != null ? 
                    review.getBikers().getFullName() : null)
                .foodRating(review.getFoodRating())
                .deliveryRating(review.getDeliveryRating())
                .overallRating(review.getOverallRating())
                .reviewText(review.getReviewText())
                .reviewImages(reviewImages)
                .isAnonymous(review.isAnonymous())
                .isVerified(review.isVerified())
                .helpfulCount(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .responseText(responseText)
                .responseDate(responseDate)
                .build();
    }
}
