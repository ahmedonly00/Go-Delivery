package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.order.OrderStatusUpdate;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.OrderMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.BikersRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashierService {
    
    private final OrderRepository orderRepository;
    private final BikersRepository bikersRepository;
    private final OrderMapper orderMapper;
    private final OrderStatusUpdateService statusUpdateService;

    @Transactional
    public OrderResponse acceptOrder(Long orderId, Integer estimatedPrepTimeMinutes) {
        log.info("Accepting order ID: {} with estimated prep time: {} minutes", orderId, estimatedPrepTimeMinutes);
        
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (order.getOrderStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Only PLACED orders can be accepted");
        }
        
        order.setAcceptedAt(LocalDate.now());
        order.setEstimatedPrepTimeMinutes(estimatedPrepTimeMinutes);
        
        // Use status update service to handle status change and notifications
        return statusUpdateService.updateOrderStatusWithNotification(order, OrderStatus.CONFIRMED);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderTimeline(Long orderId) {
        log.info("Fetching timeline for order ID: {}", orderId);
        return orderRepository.findByOrderId(orderId)
                .map(orderMapper::toOrderResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }
    
    @Transactional
    public OrderResponse markOrderReadyForPickup(Long orderId) {
        log.info("Marking order ID: {} as ready for pickup", orderId);
        
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (order.getOrderStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Only orders in PREPARING status can be marked as ready for pickup");
        }
        
        // Use status update service to handle status change and notifications
        return statusUpdateService.updateOrderStatusWithNotification(order, OrderStatus.READY);
    }
    
    @Transactional
    public OrderResponse confirmOrderDispatch(Long orderId) {
        log.info("Confirming dispatch for order ID: {}", orderId);
        
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (order.getOrderStatus() != OrderStatus.READY) {
            throw new IllegalStateException("Only orders in READY status can be dispatched");
        }
        
        if (order.getBikers() == null) {
            throw new IllegalStateException("Cannot dispatch order: No biker assigned");
        }
        
        // Use status update service to handle status change and notifications
        return statusUpdateService.updateOrderStatusWithNotification(order, OrderStatus.PICKED_UP);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getPendingOrders(Pageable pageable) {
        log.info("Fetching pending orders with pagination");
        return orderRepository.findAllByOrderStatus(OrderStatus.PLACED, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.info("Fetching all orders with pagination");
        return orderRepository.findAll(pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(OrderStatusUpdate statusUpdate) {
        log.info("Updating status for order ID: {} to {}", statusUpdate.getOrderId(), statusUpdate.getStatus());
        
        Order order = orderRepository.findByOrderId(statusUpdate.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + statusUpdate.getOrderId()));
        
        // Update order status
        OrderStatus newStatus = statusUpdate.getStatus();
        order.setOrderStatus(newStatus);
        
        // Update timestamps based on status
        LocalDate now = LocalDate.now();
        switch (newStatus) {
            case PLACED:
                // No specific timestamp for PLACED status
                break;
            case CONFIRMED:
                order.setOrderConfirmedAt(now);
                break;
            case PREPARING:
                order.setOrderPreparedAt(now);
                break;
            case READY:
                // No specific timestamp for READY status
                break;
            case PICKED_UP:
                order.setPickedUpAt(now);
                break;
            case DELIVERED:
                order.setDeliveredAt(now);
                break;
            case CANCELLED:
                order.setCancelledAt(now);
                break;
        }
        
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long orderId) {
        log.info("Fetching details for order ID: {}", orderId);
        return orderRepository.findByOrderId(orderId)
                .map(orderMapper::toOrderResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    @Transactional
    public OrderResponse assignToDelivery(Long orderId, Long bikerId) {
        log.info("Assigning order ID: {} to delivery person ID: {}", orderId, bikerId);
        
        // Find the order
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // Find the delivery person (biker)
        Bikers biker = bikersRepository.findByBikerId(bikerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery person not found with id: " + bikerId));
        
        // Update order with delivery person
        order.setBikers(biker);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setOrderConfirmedAt(LocalDate.now());
        
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }
}
