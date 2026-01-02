package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.service.BranchMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/branches/{branchId}/menu")
@RequiredArgsConstructor
@Tag(name = "Branch Menu", description = "Branch menu management")
@CrossOrigin("*")
public class BranchMenuController {

    private final BranchMenuService branchMenuService;

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Create menu category",
        description = "Create a new menu category for the branch"
    )
    public ResponseEntity<MenuCategoryDTO> createCategory(
            @PathVariable Long branchId,
            @RequestBody @Valid MenuCategoryDTO categoryDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating menu category '{}' for branch {} by user {}", 
                categoryDTO.getCategoryName(), branchId, userDetails.getUsername());
        
        MenuCategoryDTO createdCategory = branchMenuService.createMenuCategory(
                branchId, categoryDTO);
        
        return ResponseEntity.ok(createdCategory);
    }

    @PostMapping("/categories/{categoryId}/items")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Create menu item",
        description = "Create a new menu item in a category"
    )
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long categoryId,
            @RequestPart("itemData") @Valid MenuItemRequest menuItemRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating menu item '{}' for category {} in branch {} by user {}", 
                menuItemRequest.getMenuItemName(), categoryId, branchId, userDetails.getUsername());
        
        MenuItemResponse createdItem = branchMenuService.createMenuItem(
                branchId, categoryId, menuItemRequest, imageFile);
        
        return ResponseEntity.ok(createdItem);
    }

    @GetMapping("/categories")
    @Operation(
        summary = "Get menu categories",
        description = "Get all menu categories for a branch"
    )
    public ResponseEntity<List<MenuCategoryDTO>> getCategories(@PathVariable Long branchId) {
        log.info("Fetching menu categories for branch {}", branchId);
        
        List<MenuCategoryDTO> categories = branchMenuService.getBranchMenuCategories(branchId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{categoryId}/items")
    @Operation(
        summary = "Get menu items",
        description = "Get all menu items in a category"
    )
    public ResponseEntity<List<MenuItemResponse>> getMenuItems(
            @PathVariable Long branchId,
            @PathVariable Long categoryId) {
        
        log.info("Fetching menu items for category {} in branch {}", categoryId, branchId);
        
        List<MenuItemResponse> items = branchMenuService.getBranchMenuItems(branchId, categoryId);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Update menu category",
        description = "Update a menu category"
    )
    public ResponseEntity<MenuCategoryDTO> updateCategory(
            @PathVariable Long branchId,
            @PathVariable Long categoryId,
            @RequestBody @Valid MenuCategoryDTO categoryDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating menu category {} for branch {} by user {}", 
                categoryId, branchId, userDetails.getUsername());
        
        MenuCategoryDTO updatedCategory = branchMenuService.updateMenuCategory(
                branchId, categoryId, categoryDTO);
        
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Delete menu category",
        description = "Delete a menu category"
    )
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long branchId,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Deleting menu category {} from branch {} by user {}", 
                categoryId, branchId, userDetails.getUsername());
        
        branchMenuService.deleteMenuCategory(branchId, categoryId);
        return ResponseEntity.noContent().build();
    }
}
