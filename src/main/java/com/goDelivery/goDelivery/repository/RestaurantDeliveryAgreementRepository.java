package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.RestaurantDeliveryAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantDeliveryAgreementRepository extends JpaRepository<RestaurantDeliveryAgreement, Long> {

    Optional<RestaurantDeliveryAgreement> findByRestaurant_RestaurantId(Long restaurantId);

    boolean existsByRestaurant_RestaurantId(Long restaurantId);
}
