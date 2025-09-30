package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.dtos.delivery.*;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.Bikers;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.repository.BikersRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BikerService {
    
    private final BikersRepository bikersRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    
    /**
     * Find available bikers who are online and not currently on a delivery
     */
    @Transactional(readOnly = true)
    public List<Bikers> findAvailableBikers() {
        // In a real implementation, you would query for bikers who are:
        // 1. isOnline = true
        // 2. isAvailable = true
        // 3. isActive = true
        
        log.info("Finding available bikers");
        List<Bikers> availableBikers = bikersRepository.findAll().stream()
                .filter(Bikers::isOnline)
                .filter(Bikers::isAvailable)
                .filter(Bikers::isActive)
                .toList();
        
        log.info("Found {} available bikers", availableBikers.size());
        return availableBikers;
    }
    
    /**
     * Find the nearest available biker based on location
     * @param restaurantLat Restaurant latitude
     * @param restaurantLon Restaurant longitude
     * @param maxDistanceKm Maximum distance in kilometers
     */
    @Transactional(readOnly = true)
    public Optional<Bikers> findNearestAvailableBiker(Float restaurantLat, Float restaurantLon, Double maxDistanceKm) {
        List<Bikers> availableBikers = findAvailableBikers();
        
        if (availableBikers.isEmpty()) {
            log.warn("No available bikers found");
            return Optional.empty();
        }
        
        // Find the nearest biker using Haversine formula
        return availableBikers.stream()
                .filter(biker -> {
                    double distance = calculateDistance(
                            restaurantLat, restaurantLon,
                            biker.getCurrentLatitude(), biker.getCurrentLongitude()
                    );
                    return distance <= maxDistanceKm;
                })
                .min((b1, b2) -> {
                    double d1 = calculateDistance(restaurantLat, restaurantLon, 
                            b1.getCurrentLatitude(), b1.getCurrentLongitude());
                    double d2 = calculateDistance(restaurantLat, restaurantLon, 
                            b2.getCurrentLatitude(), b2.getCurrentLongitude());
                    return Double.compare(d1, d2);
                });
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     * @return distance in kilometers
     */
    private double calculateDistance(Float lat1, Float lon1, Float lat2, Float lon2) {
        final int EARTH_RADIUS_KM = 6371;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Broadcast new order notification to all available bikers
     */
    public void broadcastOrderToAvailableBikers(Order order) {
        List<Bikers> availableBikers = findAvailableBikers();
        
        if (availableBikers.isEmpty()) {
            log.warn("No available bikers to notify for order {}", order.getOrderNumber());
            return;
        }
        
        String restaurantName = order.getRestaurant() != null ? 
                order.getRestaurant().getRestaurantName() : "Restaurant";
        String pickupAddress = order.getRestaurant() != null && order.getBranch() != null ? 
                order.getBranch().getAddress() : "Restaurant location";
        String deliveryAddress = order.getDeliveryAddress();
        
        log.info("Broadcasting order {} to {} available bikers", 
                order.getOrderNumber(), availableBikers.size());
        
        for (Bikers biker : availableBikers) {
            try {
                notificationService.notifyBikerNewOrder(
                    biker.getBikerId(),
                    biker.getEmail(),
                    biker.getPhoneNumber(),
                    order.getOrderNumber(),
                    restaurantName,
                    pickupAddress,
                    deliveryAddress
                );
            } catch (Exception e) {
                log.error("Failed to notify biker {} for order {}: {}", 
                        biker.getBikerId(), order.getOrderNumber(), e.getMessage());
            }
        }
    }
    
    /**
     * Assign a biker to an order and notify them
     */
    @Transactional
    public void assignBikerToOrder(Bikers biker, Order order) {
        String restaurantName = order.getRestaurant() != null ? 
                order.getRestaurant().getRestaurantName() : "Restaurant";
        String pickupAddress = order.getRestaurant() != null && order.getBranch() != null ? 
                order.getBranch().getAddress() : "Restaurant location";
        String deliveryAddress = order.getDeliveryAddress();
        
        // Update biker availability
        biker.setAvailable(false);
        bikersRepository.save(biker);
        
        // Notify the biker
        notificationService.notifyBikerNewOrder(
            biker.getBikerId(),
            biker.getEmail(),
            biker.getPhoneNumber(),
            order.getOrderNumber(),
            restaurantName,
            pickupAddress,
            deliveryAddress
        );
        
        log.info("Assigned biker {} to order {} and sent notification", 
                biker.getBikerId(), order.getOrderNumber());
    }
    
    /**
     * Accept delivery assignment by biker
     */
    @Transactional
    public DeliveryAcceptanceResponse acceptDelivery(DeliveryAcceptanceRequest request) {
        log.info("Biker {} accepting delivery for order {}", request.getBikerId(), request.getOrderId());
        
        // Validate biker exists and is active
        Bikers biker = bikersRepository.findById(request.getBikerId())
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + request.getBikerId()));
        
        if (!biker.isActive()) {
            throw new IllegalStateException("Biker account is not active");
        }
        
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
        
        // Check if order is in a valid state for acceptance
        if (order.getOrderStatus() != OrderStatus.CONFIRMED && order.getOrderStatus() != OrderStatus.READY) {
            throw new IllegalStateException("Order is not available for delivery acceptance. Current status: " + order.getOrderStatus());
        }
        
        // Check if order is already assigned to another biker
        if (order.getBikers() != null && !order.getBikers().getBikerId().equals(biker.getBikerId())) {
            throw new IllegalStateException("Order is already assigned to another biker");
        }
        
        // Assign biker to order
        order.setBikers(biker);
        
        // Mark biker as unavailable
        biker.setAvailable(false);
        biker.setLastActive(LocalDate.now());
        
        // Save changes
        orderRepository.save(order);
        bikersRepository.save(biker);
        
        // Notify customer and restaurant
        notificationService.notifyDeliveryAccepted(order, biker, request.getEstimatedDeliveryMinutes());
        
        log.info("Biker {} successfully accepted order {}", biker.getBikerId(), order.getOrderNumber());
        
        // Build response
        return DeliveryAcceptanceResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .bikerId(biker.getBikerId())
                .bikerName(biker.getFullName())
                .orderStatus(order.getOrderStatus())
                .restaurantName(order.getRestaurant() != null ? order.getRestaurant().getRestaurantName() : null)
                .pickupAddress(order.getBranch() != null ? order.getBranch().getAddress() : "Restaurant location")
                .deliveryAddress(order.getDeliveryAddress())
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhoneNumber() : null)
                .orderAmount(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0)
                .acceptedAt(LocalDateTime.now())
                .estimatedDeliveryMinutes(request.getEstimatedDeliveryMinutes())
                .message("Delivery accepted successfully. Please proceed to restaurant for pickup.")
                .build();
    }
    
    /**
     * Reject delivery assignment by biker
     */
    @Transactional
    public void rejectDelivery(DeliveryRejectionRequest request) {
        log.info("Biker {} rejecting delivery for order {} - Reason: {}", 
                request.getBikerId(), request.getOrderId(), request.getReason());
        
        // Validate biker exists
        Bikers biker = bikersRepository.findById(request.getBikerId())
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + request.getBikerId()));
        
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
        
        // If biker was assigned, unassign them
        if (order.getBikers() != null && order.getBikers().getBikerId().equals(biker.getBikerId())) {
            order.setBikers(null);
            orderRepository.save(order);
        }
        
        // Update biker's last active time
        biker.setLastActive(LocalDate.now());
        bikersRepository.save(biker);
        
        // Notify restaurant that biker rejected
        notificationService.notifyDeliveryRejected(order, biker, request.getReason());
        
        // Broadcast to other available bikers
        broadcastOrderToAvailableBikers(order);
        
        log.info("Biker {} rejected order {}. Order broadcast to other bikers.", 
                biker.getBikerId(), order.getOrderNumber());
    }
    
    /**
     * Get list of available orders for a biker
     */
    @Transactional(readOnly = true)
    public List<Order> getAvailableOrdersForBiker(Long bikerId) {
        // Validate biker exists
        Bikers biker = bikersRepository.findById(bikerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + bikerId));
        
        // Get orders that are CONFIRMED or READY and either unassigned or assigned to this biker
        return orderRepository.findAll().stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.CONFIRMED || 
                               order.getOrderStatus() == OrderStatus.READY)
                .filter(order -> order.getBikers() == null || 
                               order.getBikers().getBikerId().equals(bikerId))
                .toList();
    }
    
    /**
     * Get biker's active/assigned orders
     */
    @Transactional(readOnly = true)
    public List<Order> getBikerActiveOrders(Long bikerId) {
        Bikers biker = bikersRepository.findById(bikerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + bikerId));
        
        return orderRepository.findAll().stream()
                .filter(order -> order.getBikers() != null && 
                               order.getBikers().getBikerId().equals(bikerId))
                .filter(order -> order.getOrderStatus() != OrderStatus.DELIVERED && 
                               order.getOrderStatus() != OrderStatus.CANCELLED)
                .toList();
    }
    
    /**
     * Confirm pickup at restaurant
     */
    @Transactional
    public PickupConfirmationResponse confirmPickup(PickupConfirmationRequest request) {
        log.info("Biker {} confirming pickup for order {}", request.getBikerId(), request.getOrderId());
        
        // Validate biker exists and is active
        Bikers biker = bikersRepository.findById(request.getBikerId())
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + request.getBikerId()));
        
        if (!biker.isActive()) {
            throw new IllegalStateException("Biker account is not active");
        }
        
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
        
        // Verify order is assigned to this biker
        if (order.getBikers() == null || !order.getBikers().getBikerId().equals(biker.getBikerId())) {
            throw new IllegalStateException("Order is not assigned to this biker");
        }
        
        // Check if order is in a valid state for pickup (CONFIRMED or READY)
        if (order.getOrderStatus() != OrderStatus.CONFIRMED && 
            order.getOrderStatus() != OrderStatus.READY) {
            throw new IllegalStateException("Order is not ready for pickup. Current status: " + order.getOrderStatus());
        }
        
        // Update order status to PICKED_UP
        order.setOrderStatus(OrderStatus.PICKED_UP);
        order.setPickedUpAt(LocalDate.now());
        
        // Update biker's last active time
        biker.setLastActive(LocalDate.now());
        
        // Save changes
        orderRepository.save(order);
        bikersRepository.save(biker);
        
        // Notify customer that order is on the way
        notificationService.notifyPickupConfirmed(order, biker);
        
        log.info("Biker {} confirmed pickup for order {}. Status: PICKED_UP", 
                biker.getBikerId(), order.getOrderNumber());
        
        // Calculate estimated delivery time
        Integer estimatedMinutes = calculateEstimatedDeliveryTime(order, biker);
        
        // Generate navigation URL to customer address
        String navigationUrl = generateNavigationUrl(order.getDeliveryAddress());
        
        // Build response
        return PickupConfirmationResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(OrderStatus.PICKED_UP)
                .bikerId(biker.getBikerId())
                .bikerName(biker.getFullName())
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhoneNumber() : null)
                .deliveryAddress(order.getDeliveryAddress())
                .orderAmount(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0)
                .pickedUpAt(LocalDateTime.now())
                .estimatedDeliveryMinutes(estimatedMinutes)
                .message("Pickup confirmed. Navigate to customer location and complete delivery.")
                .navigationUrl(navigationUrl)
                .build();
    }
    
    /**
     * Calculate estimated delivery time based on distance
     */
    private Integer calculateEstimatedDeliveryTime(Order order, Bikers biker) {
        // In a real implementation, use Google Maps API or similar
        // For now, return a reasonable estimate based on distance
        
        // Default to 20 minutes if we can't calculate
        return 20;
    }
    
    /**
     * Generate navigation URL to customer address
     */
    private String generateNavigationUrl(String address) {
        // Generate Google Maps URL for navigation
        String encodedAddress = address.replace(" ", "+");
        return "https://www.google.com/maps/dir/?api=1&destination=" + encodedAddress;
    }
    
    /**
     * Update biker's current location
     */
    @Transactional
    public void updateLocation(LocationUpdateRequest request) {
        log.info("Updating location for biker {}: lat={}, lon={}", 
                request.getBikerId(), request.getLatitude(), request.getLongitude());
        
        Bikers biker = bikersRepository.findById(request.getBikerId())
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + request.getBikerId()));
        
        // Update biker's location
        biker.setCurrentLatitude(request.getLatitude().floatValue());
        biker.setCurrentLongitude(request.getLongitude().floatValue());
        biker.setLastActive(LocalDate.now());
        
        bikersRepository.save(biker);
        
        // Find active order for this biker
        List<Order> activeOrders = getBikerActiveOrders(request.getBikerId());
        
        if (!activeOrders.isEmpty()) {
            Order order = activeOrders.get(0);
            
            // Calculate distance to customer
            if (order.getOrderStatus() == OrderStatus.PICKED_UP) {
                // Biker is en route to customer
                notificationService.notifyCustomerLocationUpdate(order, biker, request);
            }
        }
        
        log.info("Location updated for biker {}", request.getBikerId());
    }
    
    /**
     * Get navigation details for order
     */
    @Transactional(readOnly = true)
    public NavigationResponse getNavigation(NavigationRequest request) {
        log.info("Getting navigation for biker {} and order {}", request.getBikerId(), request.getOrderId());
        
        // Validate biker
        Bikers biker = bikersRepository.findById(request.getBikerId())
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + request.getBikerId()));
        
        // Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
        
        // Verify order is assigned to this biker
        if (order.getBikers() == null || !order.getBikers().getBikerId().equals(biker.getBikerId())) {
            throw new IllegalStateException("Order is not assigned to this biker");
        }
        
        String destinationType;
        String destinationAddress;
        Double destLat = null;
        Double destLon = null;
        
        // Determine destination based on order status or navigation type
        if ("RESTAURANT".equals(request.getNavigationType()) || 
            order.getOrderStatus() == OrderStatus.CONFIRMED || 
            order.getOrderStatus() == OrderStatus.READY) {
            // Navigate to restaurant
            destinationType = "RESTAURANT";
            destinationAddress = order.getBranch() != null ? 
                    order.getBranch().getAddress() : "Restaurant location";
            // In production, get restaurant lat/lon from database
        } else {
            // Navigate to customer
            destinationType = "CUSTOMER";
            destinationAddress = order.getDeliveryAddress();
            // In production, geocode customer address to get lat/lon
        }
        
        // Calculate distance and ETA
        Double currentLat = biker.getCurrentLatitude() != null ? biker.getCurrentLatitude().doubleValue() : 0.0;
        Double currentLon = biker.getCurrentLongitude() != null ? biker.getCurrentLongitude().doubleValue() : 0.0;
        
        // For now, use approximate values. In production, use Google Maps API
        Double distanceKm = 3.5; // Mock distance
        Integer estimatedMinutes = 15; // Mock ETA
        
        // Generate navigation URLs
        String encodedAddress = destinationAddress.replace(" ", "+");
        String googleMapsUrl = "https://www.google.com/maps/dir/?api=1&destination=" + encodedAddress;
        String wazeUrl = "https://waze.com/ul?q=" + encodedAddress + "&navigate=yes";
        
        // Build route info
        NavigationResponse.RouteInfo routeInfo = NavigationResponse.RouteInfo.builder()
                .totalDistanceKm(distanceKm)
                .totalTimeMinutes(estimatedMinutes)
                .summary("Fastest route via main roads")
                .warnings("Traffic may affect arrival time")
                .build();
        
        return NavigationResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .destinationType(destinationType)
                .destinationAddress(destinationAddress)
                .destinationLatitude(destLat)
                .destinationLongitude(destLon)
                .currentLatitude(currentLat)
                .currentLongitude(currentLon)
                .distanceKm(distanceKm)
                .estimatedTimeMinutes(estimatedMinutes)
                .navigationUrl(googleMapsUrl)
                .googleMapsUrl(googleMapsUrl)
                .wazeUrl(wazeUrl)
                .routeInfo(routeInfo)
                .build();
    }
    
    /**
     * Start navigation to destination
     */
    @Transactional
    public NavigationResponse startNavigation(Long bikerId, Long orderId, String destinationType) {
        log.info("Biker {} starting navigation to {} for order {}", bikerId, destinationType, orderId);
        
        NavigationRequest request = NavigationRequest.builder()
                .bikerId(bikerId)
                .orderId(orderId)
                .navigationType(destinationType)
                .build();
        
        NavigationResponse navigation = getNavigation(request);
        
        // Log navigation start
        log.info("Navigation started for biker {} to {}: distance={}km, ETA={}min", 
                bikerId, destinationType, navigation.getDistanceKm(), navigation.getEstimatedTimeMinutes());
        
        return navigation;
    }
    
    /**
     * Get delivery tracking info
     */
    @Transactional(readOnly = true)
    public DeliveryTrackingResponse getDeliveryTracking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        DeliveryTrackingResponse response = new DeliveryTrackingResponse();
        response.setOrderId(orderId.toString());
        response.setCurrentStatus(order.getOrderStatus().toString());
        response.setDelivered(order.getOrderStatus() == OrderStatus.DELIVERED);
        
        if (order.getBikers() != null) {
            Bikers biker = order.getBikers();
            response.setDeliveryPersonId(biker.getBikerId().toString());
            response.setDeliveryPersonName(biker.getFullName());
            response.setDeliveryPersonPhone(biker.getPhoneNumber());
            response.setCurrentLatitude(biker.getCurrentLatitude() != null ? biker.getCurrentLatitude().doubleValue() : null);
            response.setCurrentLongitude(biker.getCurrentLongitude() != null ? biker.getCurrentLongitude().doubleValue() : null);
        }
        
        // Add status history
        java.util.List<DeliveryTrackingResponse.DeliveryStatusHistory> history = new java.util.ArrayList<>();
        
        if (order.getOrderPlacedAt() != null) {
            history.add(new DeliveryTrackingResponse.DeliveryStatusHistory(
                    "PLACED", order.getOrderPlacedAt().atStartOfDay(), null, "Order placed by customer", null, null));
        }
        if (order.getOrderConfirmedAt() != null) {
            history.add(new DeliveryTrackingResponse.DeliveryStatusHistory(
                    "CONFIRMED", order.getOrderConfirmedAt().atStartOfDay(), null, "Restaurant confirmed order", null, null));
        }
        if (order.getPickedUpAt() != null) {
            history.add(new DeliveryTrackingResponse.DeliveryStatusHistory(
                    "PICKED_UP", order.getPickedUpAt().atStartOfDay(), null, "Biker picked up order", null, null));
        }
        if (order.getDeliveredAt() != null) {
            history.add(new DeliveryTrackingResponse.DeliveryStatusHistory(
                    "DELIVERED", order.getDeliveredAt().atStartOfDay(), null, "Order delivered to customer", null, null));
            response.setDeliveredAt(order.getDeliveredAt().atStartOfDay());
        }
        
        response.setStatusHistory(history);
        
        return response;
    }
    
    /**
     * Confirm successful delivery
     */
    @Transactional
    public DeliveryConfirmationResponse confirmDelivery(DeliveryConfirmationRequest request) {
        log.info("Biker {} confirming delivery for order {}", request.getBikerId(), request.getOrderId());
        
        // Validate biker exists and is active
        Bikers biker = bikersRepository.findById(request.getBikerId())
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + request.getBikerId()));
        
        if (!biker.isActive()) {
            throw new IllegalStateException("Biker account is not active");
        }
        
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
        
        // Verify order is assigned to this biker
        if (order.getBikers() == null || !order.getBikers().getBikerId().equals(biker.getBikerId())) {
            throw new IllegalStateException("Order is not assigned to this biker");
        }
        
        // Check if order is in a valid state for delivery (PICKED_UP)
        if (order.getOrderStatus() != OrderStatus.PICKED_UP) {
            throw new IllegalStateException("Order is not ready for delivery confirmation. Current status: " + order.getOrderStatus());
        }
        
        // Update order status to DELIVERED
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDate.now());
        
        // Store delivery proof details
        if (request.getRecipientName() != null) {
            // In production, store in order.recipientName field
            log.info("Delivery received by: {}", request.getRecipientName());
        }
        
        if (request.getDeliveryProofImage() != null) {
            // In production, store image in file storage (S3, etc.) and save URL
            log.info("Delivery proof image captured for order {}", order.getOrderNumber());
        }
        
        if (request.getRecipientSignature() != null) {
            // In production, store signature in file storage
            log.info("Customer signature captured for order {}", order.getOrderNumber());
        }
        
        if (request.getDeliveryLatitude() != null && request.getDeliveryLongitude() != null) {
            // Store delivery location for verification
            log.info("Delivery location recorded: lat={}, lon={}", 
                    request.getDeliveryLatitude(), request.getDeliveryLongitude());
        }
        
        // Mark biker as available again
        biker.setAvailable(true);
        biker.setLastActive(LocalDate.now());
        
        // Update delivery stats
        if (biker.getTotalDeliveries() != null) {
            biker.setTotalDeliveries(biker.getTotalDeliveries() + 1);
        } else {
            biker.setTotalDeliveries(1);
        }
        
        // Save changes
        orderRepository.save(order);
        bikersRepository.save(biker);
        
        // Notify customer and restaurant
        notificationService.notifyDeliveryCompleted(order, biker, request);
        
        log.info("Biker {} successfully delivered order {}. Biker now available.", 
                biker.getBikerId(), order.getOrderNumber());
        
        // Calculate earnings (in production, get from payment system)
        Double deliveryFee = 5.0; // Mock delivery fee
        Double tip = 0.0; // Mock tip (could come from order or request)
        Double totalEarnings = deliveryFee + tip;
        
        DeliveryConfirmationResponse.DeliveryEarnings earnings = 
                DeliveryConfirmationResponse.DeliveryEarnings.builder()
                        .deliveryFee(deliveryFee)
                        .tip(tip)
                        .totalEarnings(totalEarnings)
                        .paymentStatus("PENDING")
                        .build();
        
        // Build response
        return DeliveryConfirmationResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .orderStatus("DELIVERED")
                .bikerId(biker.getBikerId())
                .bikerName(biker.getFullName())
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .orderAmount(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0)
                .deliveredAt(LocalDateTime.now())
                .recipientName(request.getRecipientName())
                .contactlessDelivery(request.getContactlessDelivery())
                .message("Delivery confirmed successfully. Thank you for your service!")
                .earnings(earnings)
                .build();
    }
    
    /**
     * Get customer interaction details for delivery
     */
    @Transactional(readOnly = true)
    public CustomerInteractionDetails getCustomerInteractionDetails(Long orderId, Long bikerId) {
        log.info("Getting customer interaction details for order {} and biker {}", orderId, bikerId);
        
        // Validate biker
        Bikers biker = bikersRepository.findById(bikerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + bikerId));
        
        // Validate order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // Verify order is assigned to this biker
        if (order.getBikers() == null || !order.getBikers().getBikerId().equals(biker.getBikerId())) {
            throw new IllegalStateException("Order is not assigned to this biker");
        }
        
        // Parse delivery address for apartment/building info
        String deliveryAddress = order.getDeliveryAddress();
        String apartmentUnit = null;
        String buildingNumber = null;
        
        // Extract apartment/unit from address (simple parsing)
        if (deliveryAddress != null) {
            if (deliveryAddress.toLowerCase().contains("apartment") || 
                deliveryAddress.toLowerCase().contains("apt")) {
                // Extract apartment number - simple regex would help here
                apartmentUnit = "Check address for unit number";
            }
        }
        
        // Generate personalized messages
        String customerName = order.getCustomer() != null ? 
                order.getCustomer().getFullName() : "Customer";
        String restaurantName = order.getRestaurant() != null ? 
                order.getRestaurant().getRestaurantName() : "the restaurant";
        
        String arrivalMessage = String.format(
                "Hi %s! This is %s, your delivery driver. I'm 2 minutes away with your order from %s.",
                customerName, biker.getFullName(), restaurantName
        );
        
        String completionMessage = String.format(
                "Hi %s! Your order from %s has been delivered. Enjoy your meal!",
                customerName, restaurantName
        );
        
        // Get order items (simplified - in production, fetch from order items table)
        List<CustomerInteractionDetails.OrderItem> items = new java.util.ArrayList<>();
        // Note: In production, iterate through order.getOrderItems() to build this list
        
        return CustomerInteractionDetails.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .customerName(customerName)
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhoneNumber() : null)
                .customerEmail(order.getCustomer() != null ? order.getCustomer().getEmail() : null)
                .deliveryAddress(deliveryAddress)
                .apartmentUnit(apartmentUnit)
                .buildingNumber(buildingNumber)
                .specialInstructions(order.getSpecialInstructions())
                .contactlessDelivery(order.getSpecialInstructions() != null && 
                        order.getSpecialInstructions().toLowerCase().contains("contactless"))
                .preferredContactMethod("TEXT") // Default, in production get from customer preferences
                .leaveAtDoor(order.getSpecialInstructions() != null && 
                        order.getSpecialInstructions().toLowerCase().contains("leave at door"))
                .gateCode(null) // Extract from special instructions if present
                .items(items)
                .totalAmount(order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0)
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : "PREPAID")
                .paymentCompleted(true) // In production, check payment status
                .arrivalMessage(arrivalMessage)
                .completionMessage(completionMessage)
                .build();
    }
}
