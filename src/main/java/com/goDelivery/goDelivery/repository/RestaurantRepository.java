package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    boolean existsByEmail(String email);
    Optional<Restaurant> findByRestaurantId(Long restaurantId);
}
