package com.goDelivery.goDelivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.Promotion;
import com.goDelivery.goDelivery.model.Restaurant;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByPromotionId(Long promotionId);
    Optional<Promotion> findByPromoCode(String promoCode);
    Optional<Promotion> findByRestaurant(Restaurant restaurant);

    
}
