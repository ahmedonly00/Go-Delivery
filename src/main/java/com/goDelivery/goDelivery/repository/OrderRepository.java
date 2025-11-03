package com.goDelivery.goDelivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(Long orderId);
    List<Order> findAllByCustomerCustomerId(Long customerId);
    List<Order> findAllByRestaurantRestaurantId(Long restaurantId);
    List<Order> findAllByBikersBikerId(Long bikerId);
    List<Order> findAllByOrderStatus(OrderStatus orderStatus);
    
    Page<Order> findAllByOrderStatus(OrderStatus orderStatus, Pageable pageable);
    
    // Optimized count query for total orders (better performance than .size())
    long countByRestaurant_RestaurantId(Long restaurantId);
    
}
