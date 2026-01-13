package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.dtos.order.OrderRequest;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.order.OrderStatusUpdate;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.OrderMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.dtos.order.OrderTrackingResponse;
import com.goDelivery.goDelivery.repository.BikersRepository;
import com.goDelivery.goDelivery.repository.BranchUsersRepository;
import com.goDelivery.goDelivery.repository.BranchesRepository;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import com.goDelivery.goDelivery.repository.MenuItemRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderMapper orderMapper;
    private final BikersRepository bikersRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final BranchesRepository branchesRepository;

    @Transactional
    public List<OrderResponse> createOrder(OrderRequest orderRequest) {
        log.info("Starting order creation for customer ID: {}", orderRequest.getCustomerId());
        
        // Validate order items exist
        if (orderRequest.getRestaurantOrders() == null || orderRequest.getRestaurantOrders().isEmpty()) {
            log.error("Order validation failed: No restaurant orders provided");
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        
        // Validate customer exists
        log.debug("Looking up customer with ID: {}", orderRequest.getCustomerId());
        Customer customer = customerRepository.findByCustomerId(orderRequest.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + orderRequest.getCustomerId()));

        // Collect all menu item IDs to fetch them in a single query
        List<Long> menuItemIds = orderRequest.getRestaurantOrders().stream()
                .flatMap(restaurantOrder -> restaurantOrder.getOrderItems().stream())
                .map(OrderRequest.OrderItemRequest::getMenuItemId)
                .distinct()
                .collect(Collectors.toList());
        
        log.debug("Found {} unique menu item IDs to fetch", menuItemIds.size());
        
        // Fetch all menu items in one query to avoid N+1 problem
        List<MenuItem> allMenuItems = menuItemRepository.findByMenuItemIdIn(menuItemIds);
        log.debug("Retrieved {} menu items from database", allMenuItems.size());
        
        if (allMenuItems.size() != menuItemIds.size()) {
            log.error("Menu item count mismatch. Expected: {}, Found: {}", menuItemIds.size(), allMenuItems.size());
        }
        
        Map<Long, MenuItem> menuItemMap = allMenuItems.stream()
                .collect(Collectors.toMap(MenuItem::getMenuItemId, item -> item));

        List<OrderResponse> createdOrders = new ArrayList<>();
        String parentOrderNumber = generateParentOrderNumber(); // Generate a common prefix for all related orders
        log.info("Processing {} restaurant orders", orderRequest.getRestaurantOrders().size());

        for (int i = 0; i < orderRequest.getRestaurantOrders().size(); i++) {
            OrderRequest.RestaurantOrderRequest restaurantOrder = orderRequest.getRestaurantOrders().get(i);
            log.debug("Processing restaurant order {} for restaurant ID: {}", i+1, restaurantOrder.getRestaurantId());
            
            // Create a new order for each restaurant
            OrderResponse orderResponse = createSingleRestaurantOrder(
                    orderRequest, 
                    restaurantOrder, 
                    customer,
                    parentOrderNumber + "-" + (i + 1), // Append index to make order numbers unique
                    menuItemMap // Pass the pre-fetched menu items
            );
            createdOrders.add(orderResponse);
            log.debug("Created order with number: {}", orderResponse.getOrderNumber());
        }

        log.info("Successfully created {} orders", createdOrders.size());
        return createdOrders;
    }

    private OrderResponse createSingleRestaurantOrder(
            OrderRequest orderRequest, 
            OrderRequest.RestaurantOrderRequest restaurantOrder,
            Customer customer,
            String orderNumber,
            Map<Long, MenuItem> menuItemMap
    ) {
        // Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantOrder.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantOrder.getRestaurantId()));

        // Get delivery fee from restaurant if not provided in request
        Float deliveryFee = restaurantOrder.getDeliveryFee();
        if (deliveryFee == null) {
            deliveryFee = restaurant.getDeliveryFee();
            if (deliveryFee == null) {
                deliveryFee = 0.0f; // Default if restaurant has no delivery fee set
            }
        }

        // Validate branch exists (if provided)
        Branches branch = null;
        if (restaurantOrder.getBranchId() != null) {
            branch = branchesRepository.findByBranchId(restaurantOrder.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + restaurantOrder.getBranchId()));
        }

        // Calculate total amount using pre-fetched menu items
        double totalAmount = restaurantOrder.getOrderItems().stream()
                .mapToDouble(orderItem -> {
                    MenuItem menuItem = menuItemMap.get(orderItem.getMenuItemId());
                    if (menuItem == null) {
                        throw new ResourceNotFoundException("Menu item not found with id: " + orderItem.getMenuItemId());
                    }
                    return menuItem.getPrice() * orderItem.getQuantity();
                })
                .sum();

        // Create order items list using pre-fetched menu items
        List<OrderItem> orderItems = restaurantOrder.getOrderItems().stream()
                .map(item -> {
                    MenuItem menuItem = menuItemMap.get(item.getMenuItemId());
                    if (menuItem == null) {
                        throw new ResourceNotFoundException("Menu item not found with id: " + item.getMenuItemId());
                    }
                    
                    OrderItem orderItem = new OrderItem();
                    orderItem.setMenuItem(menuItem);
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setUnitPrice(menuItem.getPrice());
                    orderItem.setTotalPrice(menuItem.getPrice() * item.getQuantity());
                    orderItem.setSpecialRequests(item.getSpecialInstructions());
                    orderItem.setCreatedAt(LocalDate.now());
                    
                    // Set the order reference (will be set after order is saved)
                    // Note: We'll set this after saving the order
                    
                    return orderItem;
                })
                .collect(Collectors.toList());

        // Create order with all details
        Order order = new Order();
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setBranch(branch);
        order.setOrderStatus(orderRequest.getOrderStatus() != null ? orderRequest.getOrderStatus() : OrderStatus.PLACED);
        order.setPaymentStatus(orderRequest.getPaymentStatus() != null ? orderRequest.getPaymentStatus() : PaymentStatus.PENDING);
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setDeliveryAddress(orderRequest.getDeliveryAddress());
        order.setSpecialInstructions(orderRequest.getSpecialInstructions());
        order.setCancellationReason(orderRequest.getCancellationReason());
        order.setOrderPlacedAt(LocalDate.now());
        order.setCreatedAt(LocalDate.now());
        order.setUpdatedAt(LocalDate.now());
        order.setSubTotal((float) totalAmount);
        order.setDiscountAmount(orderRequest.getDiscountAmount() != null ? orderRequest.getDiscountAmount() : 0.0f);
        order.setDeliveryFee(deliveryFee); // Use the calculated delivery fee
        
        // Initialize other nullable fields to avoid issues
        order.setAcceptedAt(null);
        order.setEstimatedPrepTimeMinutes(null);
        order.setActualPrepCompletedAt(null);
        order.setOrderConfirmedAt(null);
        order.setOrderPreparedAt(null);
        order.setPickedUpAt(null);
        order.setDeliveredAt(null);
        order.setCancelledAt(null);
        order.setPaymentCompletedAt(null);
        order.setPaymentFailureReason(null);
        order.setDisbursementReference(null);
        order.setDisbursementStatus(null);
        order.setDisbursementCompletedAt(null);
        order.setBikers(null);
        order.setPromotion(null);

        // Calculate final amount: subtotal + delivery fee - discount
        float finalAmount = (float) totalAmount + deliveryFee - (orderRequest.getDiscountAmount() != null ? orderRequest.getDiscountAmount() : 0.0f);
        order.setFinalAmount(finalAmount);
        order.setOrderNumber(orderNumber);

        // Set order reference on each OrderItem before adding to order
        orderItems.forEach(orderItem -> orderItem.setOrder(order));

        // Set order items - cascade will save them
        order.setOrderItems(orderItems);

        // Save the order - cascade will save the order items
        Order savedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponse(savedOrder);
    }

    
    //Generates a parent order number for multi-restaurant orders
    private String generateParentOrderNumber() {
        LocalDate now = LocalDate.now();
        String datePart = String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String randomPart = String.format("%04d", (int)(Math.random() * 10000));
        return "BULK-" + datePart + "-" + randomPart;
    }

    
    //Generates a unique order number
    private String generateOrderNumber(Long orderId) {
        LocalDate now = LocalDate.now();
        String datePart = String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String orderIdPart = String.format("%05d", orderId);
        return "ORD-" + datePart + "-" + orderIdPart;
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId)
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
        // Verify the authenticated user owns this restaurant
        verifyRestaurantAccess(restaurantId);
        
        List<Order> orders = orderRepository.findAllByRestaurantRestaurantId(restaurantId);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByBranch(Long branchId) {
        // Verify the authenticated user has access to this branch
        verifyBranchAccess(branchId);
        
        List<Order> orders = orderRepository.findAllByBranch_BranchId(branchId);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdate statusUpdate) {
        Order order = orderRepository.findByOrderId(orderId)
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
        Order order = orderRepository.findByOrderId(orderId)
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
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Get delivery person info if assigned
        String deliveryPersonName = null;
        String deliveryPersonContact = null;
        Double deliveryPersonRating = null;
        
        if (order.getBikers() != null) {
            Bikers biker = bikersRepository.findByBikerId(order.getBikers().getBikerId())
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
            history.add(createStatusUpdate(OrderStatus.PREPARING, "Restaurant is preparing your food", order.getOrderPreparedAt()));
        }
        
        if (order.getOrderStatus().ordinal() >= OrderStatus.READY.ordinal()) {
            history.add(createStatusUpdate(OrderStatus.READY, "Your order is ready for pickup", order.getOrderPreparedAt()));
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
    
    @Transactional(readOnly = true)
    public long getTotalOrdersByRestaurant(Long restaurantId) {
        // Verify the authenticated user owns this restaurant
        verifyRestaurantAccess(restaurantId);
        
        return orderRepository.countByRestaurant_RestaurantId(restaurantId);
    }
    
    @Transactional(readOnly = true)
    public long getTotalOrdersByBranch(Long branchId) {
        // Verify the authenticated user has access to this branch
        verifyBranchAccess(branchId);
        
        return orderRepository.countByBranch_BranchId(branchId);
    }
    
    @Transactional(readOnly = true)
    public RestaurantOrderStats getRestaurantOrderStats(Long restaurantId) {
        // Verify the authenticated user owns this restaurant
        verifyRestaurantAccess(restaurantId);
        
        long totalOrders = orderRepository.countByRestaurant_RestaurantId(restaurantId);
        List<Order> orders = orderRepository.findAllByRestaurantRestaurantId(restaurantId);
        
        long completedOrders = orders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
            .count();
            
        long cancelledOrders = orders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED)
            .count();
            
        long pendingOrders = orders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatus.PLACED || 
                         o.getOrderStatus() == OrderStatus.CONFIRMED ||
                         o.getOrderStatus() == OrderStatus.PREPARING)
            .count();
        
        return new RestaurantOrderStats(totalOrders, completedOrders, cancelledOrders, pendingOrders);
    }
    
    private void verifyRestaurantAccess(Long restaurantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        
        String username = authentication.getName();
        
        // Find the restaurant associated with the authenticated user
        Restaurant restaurant = restaurantRepository.findByEmail(username)
            .orElseThrow(() -> new AccessDeniedException("No restaurant found for authenticated user"));
        
        // Verify the restaurant ID matches
        if (!restaurant.getRestaurantId().equals(restaurantId)) {
            throw new AccessDeniedException("You do not have permission to access this restaurant's data");
        }
    }
    
    private void verifyBranchAccess(Long branchId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }
        
        String username = authentication.getName();
        
        // Check if user is a restaurant admin
        Restaurant restaurant = restaurantRepository.findByEmail(username)
            .orElse(null);
        
        if (restaurant != null) {
            // Verify restaurant admin has access to this branch's restaurant
            Branches branch = branchesRepository.findByBranchId(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
            
            if (restaurant.getRestaurantId().equals(branch.getRestaurant().getRestaurantId())) {
                return; // Restaurant admin has access
            }
        }
        
        // Check if user is a branch manager
        BranchUsers branchUser = branchUsersRepository.findByEmail(username)
            .orElse(null);
        
        if (branchUser != null && branchUser.getBranch() != null) {
            if (branchUser.getBranch().getBranchId().equals(branchId)) {
                return; // Branch manager has access to their own branch
            }
        }
        
        throw new AccessDeniedException("You do not have permission to access this branch's data");
    }
    
    public static class RestaurantOrderStats {
        public final long totalOrders;
        public final long completedOrders;
        public final long cancelledOrders;
        public final long pendingOrders;
        
        public RestaurantOrderStats(long totalOrders, long completedOrders, long cancelledOrders, long pendingOrders) {
            this.totalOrders = totalOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.pendingOrders = pendingOrders;
        }
    }

    // // Helper methods for discount calculations
    // private double applyRestaurantDiscounts(Restaurant restaurant, double amount, Long promotionId) {
    //     double discount = 0.0;
        
    //     // Check if there's a specific promotion for this restaurant
    //     if (promotionId != null) {
    //         Promotion promotion = promotionRepository.findById(promotionId)
    //             .filter(p -> p.getRestaurant().equals(restaurant))
    //             .filter(p -> p.isActive() && !p.isExpired())
    //             .orElse(null);
                
    //         if (promotion != null) {
    //             if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
    //                 discount += amount * (promotion.getDiscountValue() / 100.0);
    //             } else { // FIXED_AMOUNT
    //                 discount += Math.min(promotion.getDiscountValue(), amount);
    //             }
    //         }
    //     }
        
    //     // Apply restaurant's default discount if any
    //     if (restaurant.getDefaultDiscount() > 0) {
    //         discount += amount * (restaurant.getDefaultDiscount() / 100.0);
    //     }
        
    //     return discount;
    // }
    // private double calculateDeliveryFee(Restaurant restaurant, String deliveryAddress, double orderAmount) {
    //     // Base delivery fee
    //     double fee = restaurant.getBaseDeliveryFee();
        
    //     // Free delivery for orders above certain amount
    //     if (orderAmount >= restaurant.getFreeDeliveryThreshold()) {
    //         return 0.0;
    //     }
        
    //     // Additional fee for distance (simplified example)
    //     double distance = calculateDistance(restaurant.getAddress(), deliveryAddress);
    //     if (distance > 5) { // 5km
    //         fee += (distance - 5) * restaurant.getPerKmFee();
    //     }
        
    //     return Math.min(fee, restaurant.getMaxDeliveryFee());
    // }
    // private double applyPlatformWideDiscounts(double amount, String promotionCode) {
    //     if (promotionCode == null || promotionCode.isEmpty()) {
    //         return 0.0;
    //     }
        
    //     return promotionService.validateAndApplyPromotion(promotionCode, amount);
    // }
    // private double applyCustomerDiscounts(Customer customer, double amount) {
    //     double discount = 0.0;
        
    //     // First order discount
    //     if (!orderRepository.existsByCustomer(customer)) {
    //         discount += amount * 0.1; // 10% off for first order
    //     }
        
    //     // Loyalty discount
    //     int orderCount = orderRepository.countByCustomer(customer);
    //     if (orderCount >= 10) {
    //         discount += amount * 0.05; // 5% off for loyal customers
    //     }
        
    //     // Apply maximum discount cap if needed
    //     return Math.min(discount, amount * 0.3); // Max 30% discount from customer benefits
    // }
}
