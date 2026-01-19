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
    
    // Find orders by status and where biker is not assigned
    List<Order> findByOrderStatusAndBikersIsNull(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.restaurant.restaurantId = :restaurantId AND o.paymentStatus = :paymentStatus")
    List<Order> findByRestaurant_RestaurantIdAndPaymentStatus(
        @Param("restaurantId") Long restaurantId, 
        @Param("paymentStatus") PaymentStatus paymentStatus
    );

    @Query("SELECT " +
       "COUNT(o) as total, " +
       "SUM(CASE WHEN o.orderStatus = 'PLACED' THEN 1 ELSE 0 END) as placed, " +
       "SUM(CASE WHEN o.orderStatus = 'CONFIRMED' THEN 1 ELSE 0 END) as confirmed, " +
       "SUM(CASE WHEN o.paymentStatus = 'PAID' THEN 1 ELSE 0 END) as paid, " +
       "SUM(CASE WHEN o.orderStatus = 'DELIVERED' THEN 1 ELSE 0 END) as delivered, " +
       "SUM(CASE WHEN o.orderStatus = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled " +
       "FROM Order o WHERE o.restaurant.restaurantId = :restaurantId")
    Map<String, Long> getOrderStatusCounts(@Param("restaurantId") Long restaurantId);
    
}
