package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.dtos.order.OrderRequest;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.restaurant.BranchUserDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.mapper.MenuItemMapper;
import com.goDelivery.goDelivery.mapper.OrderMapper;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchDelegationService {

    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final BranchService branchService;
    private final BranchUserService branchUserService;
    private final BranchesRepository branchesRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final OrderRepository orderRepository;
    private final BranchSecurityService branchSecurity;
    private final MenuItemMapper menuItemMapper;
    private final OrderMapper orderMapper;
    private final RestaurantMapper restaurantMapper;

    // Menu Operations
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getBranchMenu(Long branchId) {
        log.debug("Getting menu for branch {}", branchId);
        
        Branches branch = branchesRepository.findByBranchId(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        Long restaurantId = branch.getRestaurant().getRestaurantId();
        
        // Get restaurant menu as base
        List<MenuItem> restaurantMenuItems = menuItemRepository.findByRestaurant_RestaurantIdAndBranchIsNull(restaurantId);
        
        // Get branch-specific menu items (overrides)
        List<MenuItem> branchMenuItems = menuItemRepository.findByBranch_BranchId(branchId);
        
        // Merge menus (branch items override restaurant items with same name)
        List<MenuItem> mergedMenu = new ArrayList<>(restaurantMenuItems);
        
        for (MenuItem branchItem : branchMenuItems) {
            // Remove restaurant item with same name if exists
            mergedMenu.removeIf(item -> item.getMenuItemName().equals(branchItem.getMenuItemName()));
            // Add branch item
            mergedMenu.add(branchItem);
        }
        
        return menuItemMapper.toMenuItemResponse(mergedMenu);
    }

    @Transactional
    public MenuItemResponse addBranchMenuItem(Long branchId, MenuItemRequest menuItemRequest) {
        log.info("Adding menu item {} to branch {}", menuItemRequest.getMenuItemName(), branchId);
        
        Branches branch = branchesRepository.findByBranchId(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        
        // Convert MenuItemRequest to MenuItem entity
        MenuItem menuItem = new MenuItem();
        menuItem.setMenuItemName(menuItemRequest.getMenuItemName());
        menuItem.setDescription(menuItemRequest.getDescription());
        menuItem.setPrice(menuItemRequest.getPrice());
        menuItem.setImage(menuItemRequest.getImage());
        menuItem.setIngredients(menuItemRequest.getIngredients());
        menuItem.setPreparationTime(menuItemRequest.getPreparationTime());
        menuItem.setRestaurant(branch.getRestaurant());
        menuItem.setBranch(branch);
        menuItem.setCreatedAt(java.time.LocalDate.now());
        menuItem.setUpdatedAt(java.time.LocalDate.now());
        
        MenuItem savedItem = menuItemRepository.save(menuItem);
        log.info("Successfully added menu item to branch {}", branchId);
        
        return menuItemMapper.toMenuItemResponse(savedItem);
    }

    @Transactional
    public MenuItemResponse updateBranchMenuItem(Long branchId, Long menuItemId, MenuItemRequest menuItemRequest) {
        log.info("Updating menu item {} for branch {}", menuItemId, branchId);
        
        MenuItem existingItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        
        // Verify item belongs to this branch or restaurant
        if (!belongsToBranchOrRestaurant(existingItem, branchId)) {
            throw new UnauthorizedException("Menu item does not belong to this branch");
        }
        
        // Update fields
        if (menuItemRequest.getMenuItemName() != null) {
            existingItem.setMenuItemName(menuItemRequest.getMenuItemName());
        }
        if (menuItemRequest.getDescription() != null) {
            existingItem.setDescription(menuItemRequest.getDescription());
        }
        if (menuItemRequest.getPrice() != null) {
            existingItem.setPrice(menuItemRequest.getPrice());
        }
        if (menuItemRequest.getIngredients() != null) {
            existingItem.setIngredients(menuItemRequest.getIngredients());
        }
        if (menuItemRequest.getPreparationTime() != null) {
            existingItem.setPreparationTime(menuItemRequest.getPreparationTime());
        }
        if (menuItemRequest.getImage() != null) {
            existingItem.setImage(menuItemRequest.getImage());
        }
        existingItem.setUpdatedAt(java.time.LocalDate.now());
        
        MenuItem updatedItem = menuItemRepository.save(existingItem);
        
        return menuItemMapper.toMenuItemResponse(updatedItem);
    }

    // Order Operations
    @Transactional
    public List<OrderResponse> createOrderForBranch(OrderRequest request, Long branchId) {
        log.info("Creating order for branch {}", branchId);
        
        // Validate branch exists
        Branches branch = branchesRepository.findByBranchId(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        
        // Set branch context in the restaurant orders
        if (request.getRestaurantOrders() != null && !request.getRestaurantOrders().isEmpty()) {
            request.getRestaurantOrders().forEach(restaurantOrder -> {
                restaurantOrder.setBranchId(branchId);
                restaurantOrder.setRestaurantId(branch.getRestaurant().getRestaurantId());
            });
        }
        
        // Use existing order service with branch context
        List<OrderResponse> orders = orderService.createOrder(request);
        
        // Save order with branch reference
        saveOrderWithBranch(orders, branch);
        
        return orders;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getBranchOrders(Long branchId, String status) {
        log.debug("Getting orders for branch {} with status {}", branchId, status);
        
        // Validate branch access
        branchSecurity.canAccessBranch(branchId, "");
        
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orders = orderRepository.findByBranch_BranchIdAndOrderStatus(branchId, orderStatus);
        } else {
            orders = orderRepository.findAllByBranch_BranchId(branchId);
        }
        
        return orderMapper.toOrderResponseList(orders);
    }

    // User Management (Restaurant Admin only)
    @Transactional
    public BranchUserDTO createBranchUser(Long branchId, BranchUserDTO userDTO) {
        log.info("Creating branch user for branch {}", branchId);
        
        // Validate restaurant admin access
        if (!branchSecurity.canManageBranchUsers(branchId)) {
            throw new UnauthorizedException("Only restaurant admin can manage branch users");
        }
        
        return branchUserService.createBranchUser(branchId, userDTO);
    }

    @Transactional(readOnly = true)
    public List<BranchUserDTO> getBranchUsers(Long branchId) {
        log.debug("Getting users for branch {}", branchId);
        
        // Validate access
        branchSecurity.canAccessBranch(branchId, "");
        
        return branchUserService.getBranchUsers(branchId);
    }

    // Branch Information
    @Transactional(readOnly = true)
    public BranchesDTO getBranchDetails(Long branchId) {
        log.debug("Getting details for branch {}", branchId);
        
        Branches branch = branchesRepository.findByBranchId(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        return restaurantMapper.toBranchDTO(branch);
    }

    @Transactional
    public BranchesDTO updateBranchDetails(Long branchId, BranchesDTO branchDTO) {
        log.info("Updating details for branch {}", branchId);
        
        // Validate access
        branchSecurity.canAccessBranch(branchId, "");
        
        Branches existingBranch = branchesRepository.findByBranchId(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + branchId));
        
        // Update fields
        if (branchDTO.getBranchName() != null) {
            existingBranch.setBranchName(branchDTO.getBranchName());
        }
        if (branchDTO.getAddress() != null) {
            existingBranch.setAddress(branchDTO.getAddress());
        }
        if (branchDTO.getPhoneNumber() != null) {
            existingBranch.setPhoneNumber(branchDTO.getPhoneNumber());
        }
        if (branchDTO.getDescription() != null) {
            existingBranch.setDescription(branchDTO.getDescription());
        }
        if (branchDTO.getLatitude() != null) {
            existingBranch.setLatitude(branchDTO.getLatitude());
        }
        if (branchDTO.getLongitude() != null) {
            existingBranch.setLongitude(branchDTO.getLongitude());
        }
        
        Branches savedBranch = branchesRepository.save(existingBranch);
        return restaurantMapper.toBranchDTO(savedBranch);
    }

    // Helper Methods
    private boolean belongsToBranchOrRestaurant(MenuItem item, Long branchId) {
        Branches branch = branchesRepository.findById(branchId).orElse(null);
        if (branch == null) return false;
        
        Long restaurantId = branch.getRestaurant().getRestaurantId();
        
        return (item.getBranch() != null && item.getBranch().getBranchId().equals(branchId)) ||
               (item.getBranch() == null && item.getRestaurant().getRestaurantId().equals(restaurantId));
    }

    private void updateBranchFields(Branches existing, BranchesDTO update) {
        if (update.getBranchName() != null) {
            existing.setBranchName(update.getBranchName());
        }
        if (update.getAddress() != null) {
            existing.setAddress(update.getAddress());
        }
        if (update.getPhoneNumber() != null) {
            existing.setPhoneNumber(update.getPhoneNumber());
        }
        if (update.getDescription() != null) {
            existing.setDescription(update.getDescription());
        }
        if (update.getLatitude() != null) {
            existing.setLatitude(update.getLatitude());
        }
        if (update.getLongitude() != null) {
            existing.setLongitude(update.getLongitude());
        }
    }

    private void saveOrderWithBranch(List<OrderResponse> orders, Branches branch) {
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
}
