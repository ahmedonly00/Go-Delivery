package com.goDelivery.goDelivery.dtos.promotion;

import com.goDelivery.goDelivery.Enum.PromotionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Promo code is not required")
    private String promoCode;
    
    @NotNull(message = "Promotion type is not required")
    private PromotionType promotionType;
    
    @NotNull(message = "Discount percentage is not required")
    private Float discountPercentage;
    
    @NotNull(message = "Discount amount is not required")
    private Float discountAmount;
    
    @NotNull(message = "Minimum order amount is not required")
    private Float minimumOrderAmount;
    
    @NotNull(message = "Maximum discount amount is not required")
    private Float maximumDiscountAmount;
    
    @NotNull(message = "Usage limit is not required")
    private Integer usageLimit;
    
    @NotNull(message = "Usage count is not required")
    private Integer usageCount;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @NotNull(message = "Active status is required")
    private boolean isActive;
    
    @NotNull(message = "Created at date is required")
    private LocalDate createdAt;
    
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
}
