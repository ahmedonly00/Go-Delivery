package com.goDelivery.goDelivery.modules.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.modules.restaurant.model.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurant_RestaurantId(Long restaurantId);

    List<Review> findByBikers_BikerId(Long bikerId);

    List<Review> findByCustomer_CustomerId(Long customerId);

    boolean existsByOrder_OrderId(Long orderId);

    @Query("SELECT AVG(r.foodRating) FROM Review r WHERE r.restaurant.restaurantId = :restaurantId")
    Double findAverageFoodRatingByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("SELECT AVG(r.deliveryRating) FROM Review r WHERE r.bikers.bikerId = :bikerId")
    Double findAverageDeliveryRatingByBikerId(@Param("bikerId") Long bikerId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.restaurant.restaurantId = :restaurantId")
    int countByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.bikers.bikerId = :bikerId")
    int countByBikerId(@Param("bikerId") Long bikerId);
}
