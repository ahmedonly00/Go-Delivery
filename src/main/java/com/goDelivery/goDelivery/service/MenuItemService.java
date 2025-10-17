package com.goDelivery.goDelivery.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.goDelivery.goDelivery.model.Restaurant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.UpdateMenuItemRequest;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.MenuItemMapper;
import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.repository.MenuCategoryRepository;
import com.goDelivery.goDelivery.repository.MenuItemRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    
    private final MenuItemRepository menuItemRepository;
    private final MenuItemMapper menuItemMapper;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    
    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        // Find the restaurant
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + request.getRestaurantId()));
            
        // Find the category
        MenuCategory category = menuCategoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        // Create and save the menu item
        MenuItem menuItem = MenuItem.builder()
            .menuItemName(request.getMenuItemName())
            .description(request.getDescription())
            .price(request.getPrice())
            .image(request.getImage())
            .ingredients(request.getIngredients())
            .isAvailable(request.isAvailable())
            .preparationTime(request.getPreparationTime())
            .preparationScore(0) // Default score
            .restaurant(restaurant)
            .category(category)
            .createdAt(LocalDate.now())
            .updatedAt(LocalDate.now())
            .build();
            
        return menuItemMapper.toMenuItemResponse(menuItemRepository.save(menuItem));
    }
    
    @Transactional
    public MenuItemResponse updateMenuItem(Long menuItemId, UpdateMenuItemRequest request) {
        // Find existing menu item
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + menuItemId));
            
        // Update fields if they are not null in the request
        if (request.getMenuItemName() != null) {
            menuItem.setMenuItemName(request.getMenuItemName());
        }
        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            menuItem.setPrice(request.getPrice());
        }
        if (request.getImage() != null) {
            menuItem.setImage(request.getImage());
        }
        if (request.getIngredients() != null) {
            menuItem.setIngredients(request.getIngredients());
        }
        if (request.getPreparationTime() != null) {
            menuItem.setPreparationTime(request.getPreparationTime());
        }
        if (request.getCategoryId() != null) {
            MenuCategory category = menuCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            menuItem.setCategory(category);
        }
        if (request.getIsAvailable() != null) {
            menuItem.setAvailable(request.getIsAvailable());
        }
        
        menuItem.setUpdatedAt(LocalDate.now());
        
        return menuItemMapper.toMenuItemResponse(menuItemRepository.save(menuItem));
    }   
    
    @Transactional
    public void deleteMenuItem(Long menuItemId) {
        if (!menuItemRepository.existsById(menuItemId)) {
            throw new ResourceNotFoundException("Menu item not found with id: " + menuItemId);
        }
        menuItemRepository.deleteById(menuItemId);
    }
    
    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long menuItemId) {
        return menuItemRepository.findById(menuItemId)
            .map(menuItemMapper::toMenuItemResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + menuItemId));
    }
    
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAllMenuItems() {
        try {
            // Get the currently authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Find the restaurant associated with the current user
            Restaurant restaurant = restaurantRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for user: " + username));
                
            // Get menu items for the restaurant
            List<MenuItem> menuItems = menuItemRepository.findByRestaurant_RestaurantId(restaurant.getRestaurantId());
            
            if (menuItems.isEmpty()) {
                throw new ResourceNotFoundException("No menu items found for your restaurant. Please add some menu items to get started.");
            }
            
            return menuItems.stream()
                .map(menuItemMapper::toMenuItemResponse)
                .collect(Collectors.toList());
                
        } catch (ResourceNotFoundException e) {
            // Re-throw with a more specific message if it's our custom exception
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching menu items. Please try again later.", e);
        }
    }
    
    public MenuItemResponse getMenuItemByName(String itemName){
        MenuItem menuItem = menuItemRepository.findByMenuItemName(itemName)
                .orElseThrow(() -> new RuntimeException("MenuItem not found"));
        return menuItemMapper.toMenuItemResponse(menuItem);
    }
    
    @Transactional(readOnly = true)
    public List<MenuItem> getMenuItemsByRestaurantId(Long restaurantId) {
        return menuItemRepository.findByRestaurant_RestaurantId(restaurantId);
    }
    
    @Transactional(readOnly = true)
    public List<MenuItem> getAvailableMenuItemsByRestaurantId(Long restaurantId) {
        return menuItemRepository.findByRestaurant_RestaurantIdAndIsAvailableTrue(restaurantId);
    }
    
    /**
     * Maps a MenuItem entity to MenuItemResponse
     */
    public MenuItemResponse mapToResponse(MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }
        return menuItemMapper.toMenuItemResponse(menuItem);
    }
    
    @Transactional
    public MenuItemResponse updateMenuItemAvailability(Long menuItemId, boolean isAvailable) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + menuItemId));
            
        menuItem.setAvailable(isAvailable);
        menuItem.setUpdatedAt(LocalDate.now());
        
        return menuItemMapper.toMenuItemResponse(menuItemRepository.save(menuItem));
    }
            
}
