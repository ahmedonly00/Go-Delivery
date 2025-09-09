package com.goDelivery.goDelivery.dtos.promotion;

import com.goDelivery.goDelivery.Enum.PromotionType;
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
public class PromotionResponse {
    private Long promotionId;
    private String title;
    private String description;
    private String promoCode;
    private PromotionType promotionType;
    private Float discountPercentage;
    private Float discountAmount;
    private Float minimumOrderAmount;
    private Float maximumDiscountAmount;
    private Integer usageLimit;
    private Integer usageCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
    private LocalDate createdAt;
    private Long restaurantId;
    private String restaurantName;
    
    // Additional fields for response
    private List<Long> orderIds;
}
