package com.goDelivery.goDelivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.goDelivery.goDelivery.Enum.OrderStatus;
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
    
}
