package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    boolean existsByEmail(String email);
    Optional<Restaurant> findByRestaurantId(Long restaurantId);
    Optional<Restaurant> findByEmail(String email);
    List<Restaurant> findByLocation(String location);
    List<Restaurant> findByCuisineType(String cuisineType);
    List<Restaurant> findByIsActive(boolean isActive);
    List<Restaurant> findByCuisineTypeAndIsActive(String cuisineType, boolean isActive);
    
    @Query("SELECT r FROM Restaurant r WHERE " +
           "LOWER(r.restaurantName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.location) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Restaurant> searchRestaurants(@Param("query") String query);
    
    @Query("SELECT DISTINCT r FROM Restaurant r " +
           "LEFT JOIN r.promotions p " +
           "WHERE p IS NOT NULL AND p.isActive = true")
    List<Restaurant> findRestaurantsWithActivePromotions();
    
    @Query("SELECT r FROM Restaurant r WHERE r.rating >= :minRating")
    List<Restaurant> findRestaurantsWithMinRating(@Param("minRating") float minRating);
    
    @Query("SELECT r FROM Restaurant r WHERE r.deliveryFee <= :maxFee")
    List<Restaurant> findRestaurantsWithMaxDeliveryFee(@Param("maxFee") float maxFee);
    
    @Query("SELECT r FROM Restaurant r WHERE r.averagePreparationTime <= :maxTime")
    List<Restaurant> findRestaurantsWithMaxDeliveryTime(@Param("maxTime") int maxTime);
}
