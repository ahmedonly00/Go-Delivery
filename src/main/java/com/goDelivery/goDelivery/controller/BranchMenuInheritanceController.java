package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dto.menu.MenuItemPartialUpdateDTO;
import com.goDelivery.goDelivery.dto.menu.MenuProgressiveResponseDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.UpdateMenuItemRequest;
import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.service.BranchMenuInheritanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/branch-menu")
@RequiredArgsConstructor
@Tag(name = "Branch Menu Inheritance", description = "Menu inheritance and management for branches")
@CrossOrigin("*")
public class BranchMenuInheritanceController {

    private final BranchMenuInheritanceService branchMenuInheritanceService;

    @PostMapping("/{branchId}/inherit")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Inherit restaurant menu",
        description = "Copy all menu categories and items from the restaurant to the branch"
    )
    public ResponseEntity<List<MenuCategory>> inheritRestaurantMenu(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Inheriting restaurant menu for branch {} by user {}", 
                branchId, userDetails.getUsername());
        
        List<MenuCategory> inheritedMenu = branchMenuInheritanceService.inheritRestaurantMenu(branchId);
        return ResponseEntity.ok(inheritedMenu);
    }

    @GetMapping("/{branchId}")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Get branch menu",
        description = "Get all menu categories and items for the branch"
    )
    public ResponseEntity<List<MenuCategory>> getBranchMenu(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching menu for branch {} by user {}", 
                branchId, userDetails.getUsername());
        
        List<MenuCategory> menu = branchMenuInheritanceService.getBranchMenu(branchId);
        return ResponseEntity.ok(menu);
    }

    @GetMapping("/{branchId}/progressive")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Get menu progressively",
        description = "Get menu categories and items progressively for better performance"
    )
    public ResponseEntity<MenuProgressiveResponseDTO> getMenuProgressive(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryName,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching menu progressively for branch {} by user {}", 
                branchId, userDetails.getUsername());
        
        MenuProgressiveResponseDTO response = 
                branchMenuInheritanceService.getMenuProgressive(branchId, page, size, categoryName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{branchId}/categories")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Add menu category",
        description = "Add a new menu category to the branch"
    )
    public ResponseEntity<MenuCategory> addMenuCategory(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @RequestBody MenuCategoryDTO categoryDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Adding menu category '{}' to branch {} by user {}", 
                categoryDTO.getCategoryName(), branchId, userDetails.getUsername());
        
        MenuCategory category = branchMenuInheritanceService.addBranchMenuCategory(branchId, categoryDTO);
        return ResponseEntity.ok(category);
    }

    @PostMapping("/{branchId}/categories/{categoryId}/items")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Add menu item",
        description = "Add a new menu item to a category"
    )
    public ResponseEntity<MenuItem> addMenuItem(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Category ID") 
            @PathVariable Long categoryId,
            @RequestBody MenuItemRequest menuItemRequest,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        
        log.info("Adding menu item '{}' to branch {} category {} by user {}", 
                menuItemRequest.getMenuItemName(), branchId, categoryId, userDetails.getUsername());
        
        MenuItem menuItem = branchMenuInheritanceService.addBranchMenuItem(
                branchId, categoryId, menuItemRequest, request);
        return ResponseEntity.ok(menuItem);
    }

    @PutMapping("/{branchId}/items/{menuItemId}")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Update menu item",
        description = "Update an existing menu item (can modify inherited items)"
    )
    public ResponseEntity<MenuItem> updateMenuItem(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Menu Item ID") 
            @PathVariable Long menuItemId,
            @RequestBody UpdateMenuItemRequest updateRequest,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        
        log.info("Updating menu item {} for branch {} by user {}", 
                menuItemId, branchId, userDetails.getUsername());
        
        MenuItem menuItem = branchMenuInheritanceService.updateBranchMenuItem(
                branchId, menuItemId, updateRequest, request);
        return ResponseEntity.ok(menuItem);
    }

    @PatchMapping("/{branchId}/items/{menuItemId}/autosave")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Auto-save menu item",
        description = "Partially update a menu item field for auto-save functionality"
    )
    public ResponseEntity<MenuItem> autoSaveMenuItem(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Menu Item ID") 
            @PathVariable Long menuItemId,
            @RequestBody MenuItemPartialUpdateDTO partialUpdate,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        
        log.info("Auto-saving menu item {} for branch {} by user {}", 
                menuItemId, branchId, userDetails.getUsername());
        
        MenuItem menuItem = branchMenuInheritanceService.partialUpdateMenuItem(
                branchId, menuItemId, partialUpdate, request);
        return ResponseEntity.ok(menuItem);
    }

    @DeleteMapping("/{branchId}/categories/{categoryId}")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Delete menu category",
        description = "Delete a menu category from the branch"
    )
    public ResponseEntity<Void> deleteMenuCategory(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Category ID") 
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Deleting category {} from branch {} by user {}", 
                categoryId, branchId, userDetails.getUsername());
        
        branchMenuInheritanceService.deleteBranchMenuCategory(branchId, categoryId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{branchId}/items/{menuItemId}")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Delete menu item",
        description = "Delete a menu item from the branch"
    )
    public ResponseEntity<Void> deleteMenuItem(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Menu Item ID") 
            @PathVariable Long menuItemId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        
        log.info("Deleting menu item {} from branch {} by user {}", 
                menuItemId, branchId, userDetails.getUsername());
        
        branchMenuInheritanceService.deleteBranchMenuItem(branchId, menuItemId, request);
        return ResponseEntity.ok().build();
    }
}
