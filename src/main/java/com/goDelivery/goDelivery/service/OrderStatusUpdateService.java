package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.mapper.OrderMapper;
import com.goDelivery.goDelivery.model.Bikers;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusUpdateService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final BikerService bikerService;

    @Transactional
    public OrderResponse updateOrderStatusWithNotification(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        
        // Update relevant timestamps
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
                order.setActualPrepCompletedAt(now);
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
        
        // Send notification
        sendStatusUpdateNotification(updatedOrder, currentStatus, newStatus);
        
        return orderMapper.toOrderResponse(updatedOrder);
    }
    
    private void sendStatusUpdateNotification(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        try {
            // Send notification to customer
            notifyCustomer(order, oldStatus, newStatus);
            
            // Send notification to biker when order is confirmed/accepted by restaurant
            if (newStatus == OrderStatus.CONFIRMED) {
                notifyBikersForNewOrder(order);
            }
            
            log.info("Sent status update notification for order {}: {} -> {}", 
                    order.getOrderId(), oldStatus, newStatus);
                    
        } catch (Exception e) {
            log.error("Failed to send status update notification for order {}: {}", 
                    order.getOrderId(), e.getMessage(), e);
        }
    }
    
    private void notifyCustomer(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        Customer customer = order.getCustomer();
        if (customer == null) {
            log.warn("Cannot send notification: No customer associated with order {}", order.getOrderId());
            return;
        }
        
        String customerEmail = customer.getEmail();
        String message = generateStatusUpdateMessage(order, oldStatus, newStatus);
        
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("orderNumber", order.getOrderNumber());
        templateData.put("oldStatus", oldStatus.toString());
        templateData.put("newStatus", newStatus.toString());
        templateData.put("message", message);
        
        notificationService.sendEmail(
            customerEmail,
            "Order #" + order.getOrderNumber() + " Status Update",
            "order-status-update",
            templateData
        );
    }
    
    private void notifyBikersForNewOrder(Order order) {
        try {
            // If a biker is already assigned, notify them specifically
            if (order.getBikers() != null) {
                Bikers assignedBiker = order.getBikers();
                bikerService.assignBikerToOrder(assignedBiker, order);
                log.info("Notified assigned biker {} about order {}", 
                        assignedBiker.getBikerId(), order.getOrderNumber());
            } else {
                // Broadcast to all available bikers
                bikerService.broadcastOrderToAvailableBikers(order);
                log.info("Broadcast order {} to all available bikers", order.getOrderNumber());
            }
        } catch (Exception e) {
            log.error("Failed to notify bikers for order {}: {}", 
                    order.getOrderId(), e.getMessage(), e);
        }
    }
    
    private String generateStatusUpdateMessage(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        switch (newStatus) {
            case CONFIRMED:
                return String.format("Your order #%s has been confirmed and is being prepared. " +
                        "Estimated preparation time: %d minutes.", 
                        order.getOrderNumber(), order.getEstimatedPrepTimeMinutes());
                        
            case PREPARING:
                return String.format("Your order #%s is now being prepared.", order.getOrderNumber());
                
            case READY:
                return String.format("Your order #%s is ready for pickup!", order.getOrderNumber());
                
            case PICKED_UP:
                Bikers biker = order.getBikers();
                return String.format("Your order #%s has been picked up by %s and is on its way to you!", 
                        order.getOrderNumber(), 
                        biker != null ? biker.getFullName() : "our delivery partner");
                        
            case DELIVERED:
                return String.format("Your order #%s has been delivered. Enjoy your meal!", order.getOrderNumber());
                
            case CANCELLED:
                return String.format("Your order #%s has been cancelled. " +
                        "If this was unexpected, please contact support.", order.getOrderNumber());
                        
            default:
                return String.format("Your order #%s status has been updated to: %s", 
                        order.getOrderNumber(), newStatus);
        }
    }
}
