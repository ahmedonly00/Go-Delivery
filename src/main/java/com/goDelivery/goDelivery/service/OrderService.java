package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.dtos.order.OrderRequest;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.order.OrderStatusUpdate;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.OrderMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.dtos.order.OrderTrackingResponse;
import com.goDelivery.goDelivery.model.Bikers;
import com.goDelivery.goDelivery.repository.BikersRepository;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import com.goDelivery.goDelivery.repository.MenuItemRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderMapper orderMapper;
    private final BikersRepository bikersRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        // Validate customer exists
        Customer customer = customerRepository.findById(orderRequest.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + orderRequest.getCustomerId()));

        // Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(orderRequest.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + orderRequest.getRestaurantId()));

        // Create order
        Order order = orderMapper.toOrder(orderRequest, customer, restaurant);

        // Calculate total amount
        double totalAmount = orderRequest.getOrderItems().stream()
                .mapToDouble(orderItem -> {
                    MenuItem menuItem = menuItemRepository.findById(orderItem.getMenuItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + orderItem.getMenuItemId()));
                    return menuItem.getPrice() * orderItem.getQuantity();
                })
                .sum();

        order.setSubTotal((float) totalAmount);
        order.setFinalAmount((float) totalAmount); // In a real app, apply discounts, taxes, etc.

        // Save order to get the ID
        Order savedOrder = orderRepository.save(order);
        
        // Generate and set order number
        String orderNumber = generateOrderNumber(savedOrder.getOrderId());
        savedOrder.setOrderNumber(orderNumber);

        // Create order items
        List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
                .map(item -> {
                    MenuItem menuItem = menuItemRepository.findById(item.getMenuItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + item.getMenuItemId()));
                    
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(savedOrder);
                    orderItem.setMenuItem(menuItem);
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setUnitPrice(menuItem.getPrice());
                    orderItem.setTotalPrice(menuItem.getPrice() * item.getQuantity());
                    orderItem.setSpecialRequests(item.getSpecialInstructions());
                    orderItem.setCreatedAt(LocalDate.now());
                    
                    return orderItem;
                })
                .collect(Collectors.toList());

        // Set order items and save again
        savedOrder.setOrderItems(orderItems);
        Order finalOrder = orderRepository.save(savedOrder);
        
        return orderMapper.toOrderResponse(finalOrder);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findAllByCustomerCustomerId(customerId);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByRestaurant(Long restaurantId) {
        List<Order> orders = orderRepository.findAllByRestaurantRestaurantId(restaurantId);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdate statusUpdate) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        order.setOrderStatus(statusUpdate.getStatus());
        if (statusUpdate.getStatus() == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDate.now());
        }
        
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String cancellationReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // Only allow canceling if order is not already completed or cancelled
        if (order.getOrderStatus() == OrderStatus.DELIVERED || order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel an order that is already completed " + order.getOrderStatus());
        }
        
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(cancellationReason);
        order.setCancelledAt(LocalDate.now());
        
        Order cancelledOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(cancelledOrder);
    }

    @Transactional(readOnly = true)
    public OrderTrackingResponse getOrderTrackingInfo(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Get delivery person info if assigned
        String deliveryPersonName = null;
        String deliveryPersonContact = null;
        Double deliveryPersonRating = null;
        
        if (order.getBikers() != null) {
            Bikers biker = bikersRepository.findById(order.getBikers().getBikerId())
                    .orElse(null);
            if (biker != null) {
                deliveryPersonName = biker.getFullName();
                deliveryPersonContact = biker.getPhoneNumber();
                // Calculate average rating (assuming you have a reviews/ratings system)
                deliveryPersonRating = biker.getRating() != null ? biker.getRating().doubleValue() : 4.5; // Default to 4.5 if null
            }
        }

        // Calculate estimated delivery time (simplified example)
        String estimatedDeliveryTime = calculateEstimatedDeliveryTime(order);
        
        // Create status history
        List<OrderTrackingResponse.StatusUpdate> statusHistory = createStatusHistory(order);

        return OrderTrackingResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(String.valueOf(order.getOrderNumber()))
                .currentStatus(order.getOrderStatus())
                .lastUpdated(LocalDateTime.now())
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .statusHistory(statusHistory)
                .deliveryPersonName(deliveryPersonName)
                .deliveryPersonContact(deliveryPersonContact)
                .deliveryPersonRating(deliveryPersonRating)
                .distanceRemaining(calculateDistanceRemaining(order))
                .estimatedMinutesRemaining(calculateMinutesRemaining(order))
                .build();
    }

    private List<OrderTrackingResponse.StatusUpdate> createStatusHistory(Order order) {
        // In a real app, you would query a separate status_history table
        // For now, we'll create a simple history based on the current status
        List<OrderTrackingResponse.StatusUpdate> history = new java.util.ArrayList<>();
        
        // Add order placed
        history.add(createStatusUpdate(OrderStatus.PLACED, "Order has been placed", order.getOrderPlacedAt()));
        
        // Add other statuses based on current status
        if (order.getOrderStatus().ordinal() >= OrderStatus.CONFIRMED.ordinal()) {
            history.add(createStatusUpdate(OrderStatus.CONFIRMED, "Restaurant has confirmed your order", order.getOrderConfirmedAt()));
        }
        
        if (order.getOrderStatus().ordinal() >= OrderStatus.PREPARING.ordinal()) {
            history.add(createStatusUpdate(OrderStatus.PREPARING, "Restaurant is preparing your food", order.getFoodReadyAt()));
        }
        
        if (order.getOrderStatus().ordinal() >= OrderStatus.READY.ordinal()) {
            history.add(createStatusUpdate(OrderStatus.READY, "Your order is ready for pickup", order.getFoodReadyAt()));
        }
        
        if (order.getOrderStatus().ordinal() >= OrderStatus.PICKED_UP.ordinal()) {
            history.add(createStatusUpdate(OrderStatus.PICKED_UP, "Delivery person has picked up your order", order.getPickedUpAt()));
        }
        
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            history.add(createStatusUpdate(OrderStatus.DELIVERED, "Order has been delivered", order.getDeliveredAt()));
        } else if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            history.add(createStatusUpdate(OrderStatus.CANCELLED, "Order was cancelled: " + order.getCancellationReason(), order.getCancelledAt()));
        }
        
        return history.stream()
                .sorted(Comparator.comparing(OrderTrackingResponse.StatusUpdate::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    private OrderTrackingResponse.StatusUpdate createStatusUpdate(OrderStatus status, String message, LocalDate timestamp) {
        return OrderTrackingResponse.StatusUpdate.builder()
                .status(status)
                .timestamp(timestamp != null ? timestamp.atStartOfDay() : LocalDateTime.now())
                .message(message)
                .build();
    }

    private String calculateEstimatedDeliveryTime(Order order) {
        // Simplified estimation logic
        // In a real app, you'd use distance, traffic, etc.
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            return "Delivered at " + order.getDeliveredAt();
        }
        
        LocalDateTime estimatedTime = LocalDateTime.now().plusMinutes(30); // Base 30 minutes
        
        // Adjust based on status
        switch (order.getOrderStatus()) {
            case PLACED:
                estimatedTime = estimatedTime.plusMinutes(45); // +45 minutes if just placed
                break;
            case CONFIRMED:
                estimatedTime = estimatedTime.plusMinutes(30);
                break;
            case PREPARING:
                estimatedTime = estimatedTime.plusMinutes(20);
                break;
            case READY:
                estimatedTime = estimatedTime.plusMinutes(15);
                break;
            case PICKED_UP:
                estimatedTime = estimatedTime.plusMinutes(10);
                break;
            case DELIVERED:
            case CANCELLED:
                // No time to add for these statuses
                break;
        }
        
        return estimatedTime.toString();
    }

    private Double calculateDistanceRemaining(Order order) {
        // In a real app, you'd calculate actual distance using maps API
        // This is a simplified version
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            return 0.0;
        }
        
        // Return random distance for demo purposes
        return Math.random() * 10; // 0-10 km remaining
    }

    private Integer calculateMinutesRemaining(Order order) {
        // In a real app, you'd calculate this based on distance and traffic
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            return 0;
        }
        
        // Return random minutes for demo purposes
        return (int) (Math.random() * 30) + 5; // 5-35 minutes remaining
    }
    
    /**
     * Generate a unique order number based on order ID and current date
     * Format: ORD-YYYYMMDD-XXXXX
     * Example: ORD-20251022-00001
     */
    private String generateOrderNumber(Long orderId) {
        LocalDate now = LocalDate.now();
        String datePart = String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String orderIdPart = String.format("%05d", orderId);
        return "ORD-" + datePart + "-" + orderIdPart;
    }
}
