package com.goDelivery.goDelivery.modules.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private Long id;
    private Long orderId;
    private Long customerId;
    private String customerName;
    private Integer foodRating;
    private Integer deliveryRating;
    private Integer overallRating;
    private String foodReview;
    private String deliveryReview;
    private LocalDateTime createdAt;
}
