package com.goDelivery.goDelivery.modules.restaurant.dto;

import com.goDelivery.goDelivery.shared.enums.PromotionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String promoCode;
    private PromotionType promotionType;
    private Float discountPercentage;
    private Float discountAmount;
    private Float minimumOrderAmount;
    private Float maximumDiscountAmount;
    private Integer usageLimit;
    private Integer usageCount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private boolean isActive;
    private LocalDate createdAt;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
}
