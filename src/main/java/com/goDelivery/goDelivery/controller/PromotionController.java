package com.goDelivery.goDelivery.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.promotion.PromotionRequest;
import com.goDelivery.goDelivery.dtos.promotion.PromotionResponse;
import com.goDelivery.goDelivery.service.PromotionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PromotionController {
    
    private final PromotionService promotionService;
    
    @PostMapping(value = "/createPromotion", consumes = "application/json", produces = "application/json")
    public PromotionResponse createPromotion(@RequestBody PromotionRequest promotionRequest){
        return promotionService.createPromotion(promotionRequest);
    }

    @PutMapping(value = "/updatePromotion/{promotionId}", consumes = "application/json", produces = "application/json")
    public PromotionResponse updatePromotion(@PathVariable Long promotionId, @RequestBody PromotionRequest promotionRequest) {
        return promotionService.updatePromotion(promotionId, promotionRequest);
    }

    @DeleteMapping(value = "/deletePromotion/{promotionId}", produces = "application/json")
    public void deletePromotion(@PathVariable Long promotionId) {
        promotionService.deletePromotion(promotionId);
    }

    @GetMapping(value = "/getPromotion/{promotionId}", produces = "application/json")
    public PromotionResponse getPromotionById(@PathVariable Long promotionId) {
        return promotionService.getPromotionById(promotionId);
    }

    @GetMapping(value = "/getAllPromotions", produces = "application/json")
    public List<PromotionResponse> getAllPromotions(){
        return promotionService.getAllPromotions(); 
    }

    @GetMapping(value = "/getActivePromotionsByRestaurant/{restaurantId}", produces = "application/json")
    public List<PromotionResponse> getActivePromotionsByRestaurant(@PathVariable Long restaurantId) {
        return promotionService.getActivePromotionsByRestaurant(restaurantId);
    }

    @GetMapping(value = "/getAllPromotionsByRestaurant/{restaurantId}/all", produces = "application/json")
    public List<PromotionResponse> getAllPromotionsByRestaurant(@PathVariable Long restaurantId) {
        return promotionService.getAllPromotionsByRestaurant(restaurantId);
    }
    
}
