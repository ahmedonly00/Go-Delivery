package com.goDelivery.goDelivery.mapper;


import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.goDelivery.goDelivery.dtos.promotion.PromotionRequest;
import com.goDelivery.goDelivery.dtos.promotion.PromotionResponse;
import com.goDelivery.goDelivery.model.Promotion;
import com.goDelivery.goDelivery.model.Restaurant;

@Component
public class PromotionMapper {

    public List<PromotionResponse> toResponse(List<Promotion> promotions) {
        if (promotions == null) {
            return null;
        }
        return promotions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PromotionResponse toResponse(Promotion promotion) {
        if (promotion == null) {
            return null;
        }

        return PromotionResponse.builder()
                .promotionId(promotion.getPromotionId())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .promoCode(promotion.getPromoCode())
                .promotionType(promotion.getPromotionType())
                .discountPercentage(promotion.getDiscountPercentage())
                .discountAmount(promotion.getDiscountAmount())
                .minimumOrderAmount(promotion.getMinimumOrderAmount())
                .maximumDiscountAmount(promotion.getMaximumDiscountAmount())
                .usageLimit(promotion.getUsageLimit())
                .usageCount(promotion.getUsageCount())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isActive(promotion.isActive())
                .createdAt(promotion.getCreatedAt())
                .restaurantId(promotion.getRestaurant().getRestaurantId())
                .restaurantName(promotion.getRestaurant().getRestaurantName())
                .build();
    }

    public Promotion toEntity(PromotionRequest promotionRequest){
        if(promotionRequest == null){
            return null;
        }
        
        Promotion promotion = new Promotion();
        promotion.setTitle(promotionRequest.getTitle());
        promotion.setDescription(promotionRequest.getDescription());
        promotion.setPromoCode(promotionRequest.getPromoCode());
        promotion.setPromotionType(promotionRequest.getPromotionType());
        promotion.setDiscountPercentage(promotionRequest.getDiscountPercentage());
        promotion.setDiscountAmount(promotionRequest.getDiscountAmount());
        promotion.setMinimumOrderAmount(promotionRequest.getMinimumOrderAmount());
        promotion.setMaximumDiscountAmount(promotionRequest.getMaximumDiscountAmount());
        promotion.setUsageLimit(promotionRequest.getUsageLimit());
        promotion.setUsageCount(promotionRequest.getUsageCount());
        promotion.setStartDate(promotionRequest.getStartDate());
        promotion.setEndDate(promotionRequest.getEndDate());
        promotion.setActive(promotionRequest.isActive());
        promotion.setCreatedAt(promotionRequest.getCreatedAt());
        
        // Create a new Restaurant and set its ID
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(promotionRequest.getRestaurantId());
        promotion.setRestaurant(restaurant);
        
        return promotion;

    }
    
}
