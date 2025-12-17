package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Order, Long> {
    
    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.orderPlacedAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByRestaurantAndDateRange(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(o), HOUR(o.orderPlacedAt) as hour FROM Order o " +
           "WHERE o.restaurant.id = :restaurantId AND o.orderPlacedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY HOUR(o.orderPlacedAt) ORDER BY COUNT(o) DESC")
    List<Object[]> findPeakOrderHours(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT m.menuItemName, COUNT(oi) as orderCount FROM OrderItem oi " +
           "JOIN oi.menuItem m JOIN oi.order o " +
           "WHERE o.restaurant.id = :restaurantId AND o.orderPlacedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY m.menuItemName ORDER BY orderCount DESC")
    List<Object[]> findMostPopularItems(
        @Param("restaurantId") Long restaurantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
