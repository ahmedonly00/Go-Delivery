package com.goDelivery.goDelivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(Long orderId);
    List<Order> findAllByCustomerId(Long customerId);
    List<Order> findAllByBikerId(Long bikerId);
    List<Order> findAllByStatus(OrderStatus status);
    
}
