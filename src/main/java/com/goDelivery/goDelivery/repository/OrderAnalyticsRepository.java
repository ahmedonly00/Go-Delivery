package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderAnalyticsRepository extends JpaRepository<Order, Long> {
    
    // Find orders by restaurant and date range
    List<Order> findByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
            Long restaurantId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    // Find orders by restaurant and date range with pagination
    Page<Order> findByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
            Long restaurantId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    // Count orders by restaurant and date range
    Long countByRestaurant_RestaurantIdAndOrderPlacedAtBetween(
            Long restaurantId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    // Calculate total revenue by restaurant and date range
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.restaurant.id = ?1 AND o.orderPlacedAt BETWEEN ?2 AND ?3")
    Double calculateTotalRevenueByDateRange(Long restaurantId, LocalDateTime startDate, LocalDateTime endDate);

    // Count orders by status for a restaurant and date range
    @Query("SELECT o.orderStatus, COUNT(o) FROM Order o WHERE o.restaurant.id = ?1 AND o.orderPlacedAt BETWEEN ?2 AND ?3 GROUP BY o.orderStatus")
    List<Object[]> countOrdersByStatusAndRestaurantIdAndOrderPlacedAtBetween(
            Long restaurantId, 
            LocalDateTime startDate, 
            LocalDateTime endDate);
}
