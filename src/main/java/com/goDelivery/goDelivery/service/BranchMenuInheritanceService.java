package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dto.menu.MenuCategoryWithItemsDTO;
import com.goDelivery.goDelivery.dto.menu.MenuItemPartialUpdateDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.UpdateMenuItemRequest;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.ValidationException;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchMenuInheritanceService {

    private final BranchesRepository branchesRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuAuditService menuAuditService;
    private final MenuRealtimeService menuRealtimeService;

    
    //Inherits all menu categories and items from the restaurant to the branch
    @Transactional
    public List<MenuCategory> inheritRestaurantMenu(Long branchId) {
        log.info("Starting menu inheritance for branch: {}", branchId);

        // Get the branch
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        // Get the restaurant
        Restaurant restaurant = branch.getRestaurant();

        // Check if branch already has menu items
        long existingMenuCount = menuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount > 0) {
            throw new ValidationException("Branch already has menu items. Cannot inherit from restaurant.");
        }

        // Get all restaurant menu categories
        List<MenuCategory> restaurantCategories = menuCategoryRepository
                .findByRestaurantId(restaurant.getRestaurantId());

        List<MenuCategory> inheritedCategories = new ArrayList<>();

        // Copy each category and its items
        for (MenuCategory restaurantCategory : restaurantCategories) {
            // Create new category for branch
            MenuCategory branchCategory = MenuCategory.builder()
                    .categoryName(restaurantCategory.getCategoryName())
                    .branch(branch)
                    .restaurant(null) // Branch categories don't reference restaurant
                    .createdAt(LocalDate.now())
                    .build();

            // Save the category first
            branchCategory = menuCategoryRepository.save(branchCategory);

            // Copy menu items
            List<MenuItem> branchItems = new ArrayList<>();
            for (MenuItem restaurantItem : restaurantCategory.getMenuItems()) {
                MenuItem branchItem = MenuItem.builder()
                        .menuItemName(restaurantItem.getMenuItemName())
                        .description(restaurantItem.getDescription())
                        .price(restaurantItem.getPrice())
                        .image(restaurantItem.getImage())
                        .ingredients(restaurantItem.getIngredients())
                        .isAvailable(restaurantItem.isAvailable())
                        .preparationTime(restaurantItem.getPreparationTime())
                        .preparationScore(restaurantItem.getPreparationScore())
                        .branch(branch)
                        .restaurant(null) // Branch items don't reference restaurant
                        .category(branchCategory)
                        .createdAt(LocalDate.now())
                        .updatedAt(LocalDate.now())
                        .build();

                // Copy variants if any
                for (MenuItemVariant restaurantVariant : restaurantItem.getVariants()) {
                    MenuItemVariant branchVariant = MenuItemVariant.builder()
                            .variantName(restaurantVariant.getVariantName())
                            .priceModifier(restaurantVariant.getPriceModifier())
                            .menuItem(branchItem)
                            .build();
                    branchItem.getVariants().add(branchVariant);
                }

                branchItems.add(branchItem);
            }

            // Save all items for this category
            menuItemRepository.saveAll(branchItems);
            branchCategory.setMenuItems(branchItems);

            inheritedCategories.add(branchCategory);
        }

        log.info("Successfully inherited {} categories and {} items for branch: {}", 
                inheritedCategories.size(), 
                inheritedCategories.stream().mapToInt(c -> c.getMenuItems().size()).sum(),
                branchId);

        return inheritedCategories;
    }

    //Updates a menu item for a branch (can modify inherited items)
    @Transactional
    public MenuItem updateBranchMenuItem(Long branchId, Long menuItemId, 
                                       UpdateMenuItemRequest updateRequest,
                                       HttpServletRequest request) {
        // Verify the menu item belongs to the branch
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Menu item does not belong to this branch");
        }

        // Log changes before updating
        if (updateRequest.getPrice() != null && !updateRequest.getPrice().equals(menuItem.getPrice())) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, "price", 
                    menuItem.getPrice().toString(), updateRequest.getPrice().toString(), 
                    "Price update", request);
        }
        if (updateRequest.getIsAvailable() != null && !updateRequest.getIsAvailable().equals(menuItem.isAvailable())) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, "isAvailable", 
                    String.valueOf(menuItem.isAvailable()), String.valueOf(updateRequest.getIsAvailable()), 
                    "Availability change", request);
        }
        if (updateRequest.getMenuItemName() != null && !updateRequest.getMenuItemName().equals(menuItem.getMenuItemName())) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, "menuItemName", 
                    menuItem.getMenuItemName(), updateRequest.getMenuItemName(), 
                    "Name update", request);
        }
        if (updateRequest.getDescription() != null && !updateRequest.getDescription().equals(menuItem.getDescription())) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, "description", 
                    menuItem.getDescription(), updateRequest.getDescription(), 
                    "Description update", request);
        }

        // Update fields
        if (updateRequest.getMenuItemName() != null) {
            menuItem.setMenuItemName(updateRequest.getMenuItemName());
        }
        if (updateRequest.getDescription() != null) {
            menuItem.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getPrice() != null) {
            menuItem.setPrice(updateRequest.getPrice());
        }
        if (updateRequest.getImage() != null) {
            menuItem.setImage(updateRequest.getImage());
        }
        if (updateRequest.getIngredients() != null) {
            menuItem.setIngredients(updateRequest.getIngredients());
        }
        if (updateRequest.getIsAvailable() != null) {
            menuItem.setAvailable(updateRequest.getIsAvailable());
        }
        if (updateRequest.getPreparationTime() != null) {
            menuItem.setPreparationTime(updateRequest.getPreparationTime());
        }

        menuItem.setUpdatedAt(LocalDate.now());
        MenuItem savedItem = menuItemRepository.save(menuItem);
        
        // Broadcast real-time update
        menuRealtimeService.broadcastMenuItemUpdate(branchId, savedItem, getCurrentUserEmail());
        
        return savedItem;
    }

    //Partially updates a menu item for auto-save functionality
    @Transactional
    public MenuItem partialUpdateMenuItem(Long branchId, Long menuItemId,
                                        MenuItemPartialUpdateDTO partialUpdate,
                                        HttpServletRequest request) {
        // Verify the menu item belongs to the branch
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Menu item does not belong to this branch");
        }

        // Track what field is being updated for audit
        String fieldName = partialUpdate.getUpdateField();
        String oldValue = null;
        String newValue = null;

        // Update only the provided field
        if (partialUpdate.getPrice() != null) {
            oldValue = menuItem.getPrice().toString();
            newValue = partialUpdate.getPrice().toString();
            menuItem.setPrice(partialUpdate.getPrice());
        }
        if (partialUpdate.getIsAvailable() != null) {
            oldValue = String.valueOf(menuItem.isAvailable());
            newValue = String.valueOf(partialUpdate.getIsAvailable());
            menuItem.setAvailable(partialUpdate.getIsAvailable());
        }
        if (partialUpdate.getDescription() != null) {
            oldValue = menuItem.getDescription();
            newValue = partialUpdate.getDescription();
            menuItem.setDescription(partialUpdate.getDescription());
        }
        if (partialUpdate.getIngredients() != null) {
            oldValue = menuItem.getIngredients();
            newValue = partialUpdate.getIngredients();
            menuItem.setIngredients(partialUpdate.getIngredients());
        }
        if (partialUpdate.getPreparationTime() != null) {
            oldValue = menuItem.getPreparationTime().toString();
            newValue = partialUpdate.getPreparationTime().toString();
            menuItem.setPreparationTime(partialUpdate.getPreparationTime());
        }

        menuItem.setUpdatedAt(LocalDate.now());
        MenuItem savedItem = menuItemRepository.save(menuItem);

        // Log the specific field change
        if (fieldName != null && oldValue != null && newValue != null) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, fieldName, 
                    oldValue, newValue, "Auto-save", request);
        }

        // Broadcast real-time update
        menuRealtimeService.broadcastMenuItemUpdate(branchId, savedItem, getCurrentUserEmail());

        return savedItem;
    }

    //Adds a new menu item to a branch category
    @Transactional
    public MenuItem addBranchMenuItem(Long branchId, Long categoryId,
                                    MenuItemRequest menuItemRequest,
                                    HttpServletRequest request) {
        // Verify category belongs to branch
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Category does not belong to this branch");
        }

        // Check if branch has inherited menu yet (if category is not newly created)
        long existingMenuCount = menuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount == 0) {
            log.info("Branch {} has no menu, inheriting from restaurant first", branchId);
            inheritRestaurantMenu(branchId);
        }

        // Create new menu item
        MenuItem menuItem = MenuItem.builder()
                .menuItemName(menuItemRequest.getMenuItemName())
                .description(menuItemRequest.getDescription())
                .price(menuItemRequest.getPrice())
                .image(menuItemRequest.getImage())
                .ingredients(menuItemRequest.getIngredients())
                .isAvailable(menuItemRequest.isAvailable())
                .preparationTime(menuItemRequest.getPreparationTime())
                .branch(branchesRepository.findById(branchId).get())
                .category(category)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        MenuItem savedItem = menuItemRepository.save(menuItem);
        
        // Log the creation
        menuAuditService.logMenuItemCreate(savedItem.getMenuItemId(), branchId, 
                savedItem.getMenuItemName(), request);
        
        // Broadcast real-time update
        menuRealtimeService.broadcastMenuItemAdded(branchId, savedItem, getCurrentUserEmail());
        
        return savedItem;
    }

    // Adds a new menu category to a branch
    @Transactional
    public MenuCategory addBranchMenuCategory(Long branchId, 
                                             MenuCategoryDTO categoryDTO) {
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        // Check if branch has inherited menu yet
        long existingMenuCount = menuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount == 0) {
            log.info("Branch {} has no menu, inheriting from restaurant first", branchId);
            inheritRestaurantMenu(branchId);
        }

        // Check if category name already exists for this branch
        boolean exists = menuCategoryRepository.existsByBranch_BranchIdAndCategoryName(branchId, categoryDTO.getCategoryName());
        if (exists) {
            throw new ValidationException("Category with this name already exists for this branch");
        }

        MenuCategory category = MenuCategory.builder()
                .categoryName(categoryDTO.getCategoryName())
                .branch(branch)
                .createdAt(LocalDate.now())
                .build();

        return menuCategoryRepository.save(category);
    }

    
    //Gets all menu categories for a branch (including inherited ones)
    @Transactional(readOnly = true)
    public List<MenuCategory> getBranchMenu(Long branchId) {
        return menuCategoryRepository.findByBranch_BranchId(branchId);
    }
    
    //Gets menu categories and items progressively
    @Transactional(readOnly = true)
    public com.goDelivery.goDelivery.dto.menu.MenuProgressiveResponseDTO getMenuProgressive(
            Long branchId, int page, int size, String categoryName) {
        
        // First, check if branch has menu and inherit if needed
        long existingMenuCount = menuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount == 0) {
            log.info("Branch {} has no menu, inheriting from restaurant first", branchId);
            inheritRestaurantMenu(branchId);
        }
        
        // Get categories with pagination
        List<MenuCategory> categories;
        if (categoryName != null && !categoryName.isEmpty()) {
            categories = menuCategoryRepository.findByBranch_BranchIdAndCategoryNameContainingIgnoreCase(
                    branchId, categoryName);
        } else {
            categories = menuCategoryRepository.findByBranch_BranchId(branchId);
        }
        
        // Apply pagination
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, categories.size());
        List<MenuCategory> paginatedCategories = categories.subList(startIndex, endIndex);
        
        // Convert to DTOs
        List<com.goDelivery.goDelivery.dto.menu.MenuCategoryWithItemsDTO> categoryDTOs = 
                paginatedCategories.stream()
                        .map(this::convertToCategoryWithItemsDTO)
                        .toList();
        
        // Build response
        return com.goDelivery.goDelivery.dto.menu.MenuProgressiveResponseDTO.builder()
                .categories(categoryDTOs)
                .currentPage(page)
                .totalPages((int) Math.ceil((double) categories.size() / size))
                .totalItems(categories.size())
                .hasMore(endIndex < categories.size())
                .nextCursor(endIndex < categories.size() ? String.valueOf(page + 1) : null)
                .prevCursor(page > 0 ? String.valueOf(page - 1) : null)
                .build();
    }
    
    private MenuCategoryWithItemsDTO convertToCategoryWithItemsDTO(MenuCategory category) {
        List<MenuCategoryWithItemsDTO.MenuItemDTO> itemDTOs = 
                category.getMenuItems().stream()
                        .map(item -> MenuCategoryWithItemsDTO.MenuItemDTO.builder()
                                .menuItemId(item.getMenuItemId())
                                .menuItemName(item.getMenuItemName())
                                .description(item.getDescription())
                                .price(item.getPrice())
                                .image(item.getImage())
                                .ingredients(item.getIngredients())
                                .isAvailable(item.isAvailable())
                                .preparationTime(item.getPreparationTime())
                                .isInherited(true) // All items are inherited or branch-specific
                                .build())
                        .toList();
        
        return MenuCategoryWithItemsDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .items(itemDTOs)
                .isInherited(true) // All categories are inherited or branch-specific
                .itemCount(itemDTOs.size())
                .build();
    }

    
    //Deletes a menu item from a branch
    @Transactional
    public void deleteBranchMenuItem(Long branchId, Long menuItemId, 
                                   HttpServletRequest request) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Menu item does not belong to this branch");
        }

        // Log the deletion before deleting
        menuAuditService.logMenuItemDelete(menuItemId, branchId, 
                menuItem.getMenuItemName(), request);
        
        menuItemRepository.delete(menuItem);
        
        // Broadcast real-time update
        menuRealtimeService.broadcastMenuItemRemoved(branchId, menuItemId, getCurrentUserEmail());
    }
    
    private String getCurrentUserEmail() {
        try {
            // This would need to be injected or passed as parameter
            // For now, returning a placeholder
            return "current-user";
        } catch (Exception e) {
            return "system";
        }
    }

    
    //Deletes a menu category from a branch (only if it was created by the branch, not inherited)
    @Transactional
    public void deleteBranchMenuCategory(Long branchId, Long categoryId) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Category does not belong to this branch");
        }

        // Only allow deletion if category has no items or if explicitly confirmed
        if (!category.getMenuItems().isEmpty()) {
            throw new ValidationException("Cannot delete category with existing items. Remove items first.");
        }

        menuCategoryRepository.delete(category);
    }
}
