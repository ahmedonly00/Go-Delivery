package com.goDelivery.goDelivery.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.goDelivery.goDelivery.dtos.promotion.PromotionRequest;
import com.goDelivery.goDelivery.dtos.promotion.PromotionResponse;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.PromotionMapper;
import com.goDelivery.goDelivery.model.Promotion;
import com.goDelivery.goDelivery.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    private final PromotionMapper promotionMapper;

    public PromotionResponse createPromotion(PromotionRequest promotionRequest){
        Promotion promotion = promotionMapper.toEntity(promotionRequest);
        Promotion savedPromotion = promotionRepository.save(promotion);
        return promotionMapper.toResponse(savedPromotion);
        
    }

    public PromotionResponse updatePromotion(Long promotionId, PromotionRequest promotionRequest){
        Promotion existingPromotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

                existingPromotion.setTitle(promotionRequest.getTitle());
                existingPromotion.setDescription(promotionRequest.getDescription());
                existingPromotion.setPromoCode(promotionRequest.getPromoCode());
                existingPromotion.setPromotionType(promotionRequest.getPromotionType());
                existingPromotion.setDiscountPercentage(promotionRequest.getDiscountPercentage());
                existingPromotion.setDiscountAmount(promotionRequest.getDiscountAmount());
                existingPromotion.setMinimumOrderAmount(promotionRequest.getMinimumOrderAmount());
                existingPromotion.setMaximumDiscountAmount(promotionRequest.getMaximumDiscountAmount());
                existingPromotion.setUsageLimit(promotionRequest.getUsageLimit());
                existingPromotion.setUsageCount(promotionRequest.getUsageCount());
                existingPromotion.setStartDate(promotionRequest.getStartDate());
                existingPromotion.setEndDate(promotionRequest.getEndDate());
                existingPromotion.setActive(promotionRequest.isActive());
                existingPromotion.setCreatedAt(LocalDate.now());

                return promotionMapper.toResponse(promotionRepository.save(existingPromotion));
    }

    public void deletePromotion(Long promotionId){
        Promotion existingPromotion = promotionRepository.findByPromotionId(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));
        promotionRepository.delete(existingPromotion);
    }

    public PromotionResponse getPromotionById(Long promotionId){
        Promotion existingPromotion = promotionRepository.findByPromotionId(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));
        return promotionMapper.toResponse(existingPromotion);
    }

    public List<PromotionResponse> getAllPromotions(){
        List<Promotion> promotions = promotionRepository.findAll();
        return promotionMapper.toResponse(promotions);
    }

    public List<PromotionResponse> getActivePromotionsByRestaurant(Long restaurantId){
        List<Promotion> existingPromotions = promotionRepository.findByRestaurant_RestaurantIdAndIsActiveTrue(restaurantId);
        return promotionMapper.toResponse(existingPromotions);
    }

    public List<PromotionResponse> getAllPromotionsByRestaurant(Long restaurantId) {
        List<Promotion> promotions = promotionRepository.findByRestaurant_RestaurantId(restaurantId);
        return promotionMapper.toResponse(promotions);
    }      
    
}
