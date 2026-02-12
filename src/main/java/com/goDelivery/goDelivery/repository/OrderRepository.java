package com.goDelivery.goDelivery.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.model.Order;

import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

        Optional<Order> findByOrderId(Long orderId);

        List<Order> findAllByCustomerCustomerId(Long customerId);

        List<Order> findAllByRestaurantRestaurantId(Long restaurantId);

        List<Order> findAllByBikersBikerId(Long bikerId);

        List<Order> findAllByOrderStatus(OrderStatus orderStatus);

        List<Order> findAllByBranch_BranchId(Long branchId);

        List<Order> findByBranch_BranchIdAndOrderStatus(Long branchId, OrderStatus orderStatus);

        List<Order> findByRestaurant_RestaurantIdAndOrderStatus(Long restaurantId, OrderStatus orderStatus);

        Page<Order> findAllByOrderStatus(OrderStatus orderStatus, Pageable pageable);

        // Optimized count query for total orders (better performance than .size())
        long countByRestaurant_RestaurantId(Long restaurantId);

        long countByBranch_BranchId(Long branchId);

        // Find order by disbursement reference
        Optional<Order> findByDisbursementReference(String disbursementReference);

        // Find order by order number
        Optional<Order> findByOrderNumber(String orderNumber);

        // Find all orders with the same parent order number (for multi-restaurant
        // orders)
        List<Order> findByOrderNumberStartingWith(String parentOrderNumber);

        // Find orders by status and where biker is not assigned
        List<Order> findByOrderStatusAndBikersIsNull(OrderStatus status);

        @Query("SELECT o FROM Order o WHERE o.restaurant.restaurantId = :restaurantId AND o.paymentStatus = :paymentStatus")
        List<Order> findByRestaurant_RestaurantIdAndPaymentStatus(
                        @Param("restaurantId") Long restaurantId,
                        @Param("paymentStatus") PaymentStatus paymentStatus);

        @Query("SELECT " +
                        "COUNT(o) as total, " +
                        "SUM(CASE WHEN o.orderStatus = 'PLACED' THEN 1 ELSE 0 END) as placed, " +
                        "SUM(CASE WHEN o.orderStatus = 'CONFIRMED' THEN 1 ELSE 0 END) as confirmed, " +
                        "SUM(CASE WHEN o.paymentStatus = 'PAID' THEN 1 ELSE 0 END) as paid, " +
                        "SUM(CASE WHEN o.orderStatus = 'DELIVERED' THEN 1 ELSE 0 END) as delivered, " +
                        "SUM(CASE WHEN o.orderStatus = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled " +
                        "FROM Order o WHERE o.restaurant.restaurantId = :restaurantId")
        Map<String, Long> getOrderStatusCounts(@Param("restaurantId") Long restaurantId);

        // ============ Dashboard Aggregation Queries ============

        // Count orders by date range
        @Query("SELECT COUNT(o) FROM Order o WHERE o.orderPlacedAt BETWEEN :startDate AND :endDate")
        Long countOrdersByDateRange(@Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Count orders by restaurant and date range
        @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurant.restaurantId = :restaurantId " +
                        "AND o.orderPlacedAt BETWEEN :startDate AND :endDate")
        Long countOrdersByRestaurantAndDateRange(@Param("restaurantId") Long restaurantId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Count orders by status and date range
        @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status " +
                        "AND o.orderPlacedAt BETWEEN :startDate AND :endDate")
        Long countOrdersByStatusAndDateRange(@Param("status") OrderStatus status,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Sum total revenue by date range
        @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o " +
                        "WHERE o.orderStatus = 'DELIVERED' AND o.orderPlacedAt BETWEEN :startDate AND :endDate")
        Double sumRevenueByDateRange(@Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Sum total revenue by restaurant and date range
        @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o " +
                        "WHERE o.restaurant.restaurantId = :restaurantId AND o.orderStatus = 'DELIVERED' " +
                        "AND o.orderPlacedAt BETWEEN :startDate AND :endDate")
        Double sumRevenueByRestaurantAndDateRange(@Param("restaurantId") Long restaurantId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Get average order value by restaurant
        @Query("SELECT AVG(o.finalAmount) FROM Order o " +
                        "WHERE o.restaurant.restaurantId = :restaurantId AND o.orderStatus = 'DELIVERED'")
        Double getAverageOrderValueByRestaurant(@Param("restaurantId") Long restaurantId);

        // Get orders grouped by status for restaurant
        @Query("SELECT o.orderStatus, COUNT(o) FROM Order o " +
                        "WHERE o.restaurant.restaurantId = :restaurantId " +
                        "AND o.orderPlacedAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY o.orderStatus")
        List<Object[]> getOrderCountsByStatus(@Param("restaurantId") Long restaurantId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Get orders grouped by payment method
        @Query("SELECT o.paymentMethod, COUNT(o), SUM(o.finalAmount) FROM Order o " +
                        "WHERE o.orderPlacedAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY o.paymentMethod")
        List<Object[]> getOrdersByPaymentMethod(@Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Get top restaurants by revenue
        @Query("SELECT o.restaurant.restaurantId, o.restaurant.restaurantName, " +
                        "COUNT(o), SUM(o.finalAmount), AVG(o.finalAmount) " +
                        "FROM Order o WHERE o.orderStatus = 'DELIVERED' " +
                        "AND o.orderPlacedAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY o.restaurant.restaurantId, o.restaurant.restaurantName " +
                        "ORDER BY SUM(o.finalAmount) DESC")
        List<Object[]> getTopRestaurantsByRevenue(@Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate,
                        Pageable pageable);

        // Get orders by hour of day for restaurant
        @Query("SELECT HOUR(o.orderPlacedAt), COUNT(o) FROM Order o " +
                        "WHERE o.restaurant.restaurantId = :restaurantId " +
                        "AND o.orderPlacedAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY HOUR(o.orderPlacedAt) ORDER BY HOUR(o.orderPlacedAt)")
        List<Object[]> getOrdersByHourOfDay(@Param("restaurantId") Long restaurantId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Get orders by day of week for restaurant
        @Query("SELECT DAYOFWEEK(o.orderPlacedAt), COUNT(o) FROM Order o " +
                        "WHERE o.restaurant.restaurantId = :restaurantId " +
                        "AND o.orderPlacedAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY DAYOFWEEK(o.orderPlacedAt) ORDER BY DAYOFWEEK(o.orderPlacedAt)")
        List<Object[]> getOrdersByDayOfWeek(@Param("restaurantId") Long restaurantId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Get daily order counts and revenue (time series data)
        @Query("SELECT DATE(o.orderPlacedAt), COUNT(o), SUM(o.finalAmount) FROM Order o " +
                        "WHERE o.restaurant.restaurantId = :restaurantId " +
                        "AND o.orderPlacedAt BETWEEN :startDate AND :endDate " +
                        "GROUP BY DATE(o.orderPlacedAt) ORDER BY DATE(o.orderPlacedAt)")
        List<Object[]> getDailyOrdersAndRevenue(@Param("restaurantId") Long restaurantId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Count orders by biker and date range
        @Query("SELECT COUNT(o) FROM Order o WHERE o.bikers.bikerId = :bikerId " +
                        "AND o.orderPlacedAt BETWEEN :startDate AND :endDate")
        Long countOrdersByBikerAndDateRange(@Param("bikerId") Long bikerId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);

        // Sum earnings by biker
        @Query("SELECT COALESCE(SUM(o.deliveryFee), 0) FROM Order o " +
                        "WHERE o.bikers.bikerId = :bikerId AND o.orderStatus = 'DELIVERED' " +
                        "AND o.orderPlacedAt BETWEEN :startDate AND :endDate")
        Double sumEarningsByBikerAndDateRange(@Param("bikerId") Long bikerId,
                        @Param("startDate") java.time.LocalDateTime startDate,
                        @Param("endDate") java.time.LocalDateTime endDate);
}
