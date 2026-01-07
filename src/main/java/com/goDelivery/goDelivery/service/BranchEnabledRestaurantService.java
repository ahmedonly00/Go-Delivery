package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.dtos.order.OrderRequest;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.mapper.OrderMapper;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchEnabledRestaurantService {

    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final BranchesRepository branchesRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final RestaurantUsersRepository restaurantUsersRepository;
    private final BranchSecurityService branchSecurity;
    private final OrderMapper orderMapper;
    private final RestaurantMapper restaurantMapper;

    // Order Operations with Branch Support
    @Transactional
    public List<OrderResponse> createOrderForBranch(OrderRequest request, Long branchId) {
        log.info("Creating order for branch {}", branchId);
        
        // Validate branch exists
        Branches branch = branchesRepository.findByBranchId(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + branchId));
        
        // Validate branch belongs to restaurant (if restaurant ID is provided)
        if (request.getRestaurantOrders() != null && !request.getRestaurantOrders().isEmpty()) {
            request.getRestaurantOrders().forEach(restaurantOrder -> {
                if (restaurantOrder.getRestaurantId() != null && 
                    !restaurantOrder.getRestaurantId().equals(branch.getRestaurant().getRestaurantId())) {
                    throw new UnauthorizedException("Branch does not belong to the specified restaurant");
                }
                // Set branch context
                restaurantOrder.setBranchId(branchId);
                restaurantOrder.setRestaurantId(branch.getRestaurant().getRestaurantId());
            });
        }
        
        // Use existing order service with branch context
        List<OrderResponse> orders = orderService.createOrder(request);
        
        // Save order with branch reference
        saveOrderWithBranchReference(orders, branch);
        
        return orders;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByBranch(Long branchId, OrderStatus status) {
        log.debug("Getting orders for branch {} with status {}", branchId, status);
        
        // Validate branch access
        branchSecurity.canAccessBranch(branchId, "");
        
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByBranch_BranchIdAndOrderStatus(branchId, status);
        } else {
            orders = orderRepository.findAllByBranch_BranchId(branchId);
        }
        
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByRestaurant(Long restaurantId, OrderStatus status) {
        log.debug("Getting orders for restaurant {} with status {}", restaurantId, status);
        
        // Validate restaurant admin access
        RestaurantUsers currentUser = branchSecurity.getCurrentRestaurantUser();
        if (!currentUser.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new UnauthorizedException("You can only view orders for your restaurant");
        }
        
        List<Order> orders;
        if (status != null) {
            orders = orderRepository.findByRestaurant_RestaurantIdAndOrderStatus(restaurantId, status);
        } else {
            orders = orderRepository.findAllByRestaurantRestaurantId(restaurantId);
        }
        
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    // Menu Operations with Branch Support
    @Transactional
    public void createMenuItemForBranch(Long menuItemId, Long branchId) {
        log.info("Creating menu item {} for branch {}", menuItemId, branchId);
        
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        // Create a copy of the menu item for the branch
        MenuItem branchMenuItem = new MenuItem();
        branchMenuItem.setMenuItemName(menuItem.getMenuItemName());
        branchMenuItem.setDescription(menuItem.getDescription());
        branchMenuItem.setPrice(menuItem.getPrice());
        branchMenuItem.setImage(menuItem.getImage());
        branchMenuItem.setIngredients(menuItem.getIngredients());
        branchMenuItem.setPreparationTime(menuItem.getPreparationTime());
        branchMenuItem.setPreparationScore(menuItem.getPreparationScore());
        branchMenuItem.setCategory(menuItem.getCategory());
        branchMenuItem.setRestaurant(branch.getRestaurant());
        branchMenuItem.setBranch(branch);
        branchMenuItem.setCreatedAt(LocalDate.now());
        branchMenuItem.setUpdatedAt(LocalDate.now());
        
        menuItemRepository.save(branchMenuItem);
        log.info("Successfully created menu item for branch {}", branchId);
    }

    @Transactional(readOnly = true)
    public List<BranchesDTO> getRestaurantBranches(Long restaurantId) {
        log.debug("Getting branches for restaurant {}", restaurantId);
        
        // Validate access
        RestaurantUsers currentUser = branchSecurity.getCurrentRestaurantUser();
        if (!currentUser.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new UnauthorizedException("You can only view branches for your restaurant");
        }
        
        List<Branches> branches = branchesRepository.findByRestaurant_RestaurantId(restaurantId);
        
        return branches.stream()
                .map(this::convertToBranchDTO)
                .collect(Collectors.toList());
    }

    // Analytics and Reporting
    @Transactional(readOnly = true)
    public Object getBranchAnalytics(Long branchId, String reportType, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting {} analytics for branch {} from {} to {}", reportType, branchId, startDate, endDate);
        
        // Validate branch access
        branchSecurity.canAccessBranch(branchId, "");
        
        // Implementation would depend on your analytics service
        // This is a placeholder for the actual implementation
        return "Analytics data for branch " + branchId;
    }

    @Transactional(readOnly = true)
    public Object getRestaurantAnalytics(Long restaurantId, String reportType, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting {} analytics for restaurant {} from {} to {}", reportType, restaurantId, startDate, endDate);
        
        // Validate access
        RestaurantUsers currentUser = branchSecurity.getCurrentRestaurantUser();
        if (!currentUser.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new UnauthorizedException("You can only view analytics for your restaurant");
        }
        
        // Implementation would depend on your analytics service
        // This would aggregate data from all branches
        return "Analytics data for restaurant " + restaurantId;
    }

    // Helper Methods
    private void saveOrderWithBranchReference(List<OrderResponse> orders, Branches branch) {
        // This method saves orders with branch references to the database
        // Since we're working with DTOs, we need to convert them to entities first
        
        for (OrderResponse orderResponse : orders) {
            // Find the order entity by ID
            Order order = orderRepository.findById(orderResponse.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderResponse.getOrderId()));
            
            // Set the branch reference
            order.setBranch(branch);
            
            // Save the order with branch reference
            orderRepository.save(order);
            
            log.info("Successfully saved order {} for branch {}", 
                    orderResponse.getOrderId(), branch.getBranchId());
        }
    }

    private OrderResponse convertToOrderResponse(Order order) {
        // Use the existing OrderMapper
        return orderMapper.toOrderResponse(order);
    }

    private BranchesDTO convertToBranchDTO(Branches branch) {
        // Use the existing RestaurantMapper
        return restaurantMapper.toBranchDTO(branch);
    }

    // Validation Methods
    private Branches validateBranchAccess(Long branchId) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + branchId));
        
        // Additional validation can be added here
        return branch;
    }

    private void validateRestaurantAccess(Long restaurantId) {
        RestaurantUsers currentUser = branchSecurity.getCurrentRestaurantUser();
        if (!currentUser.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new UnauthorizedException("Access denied to restaurant: " + restaurantId);
        }
    }
}
