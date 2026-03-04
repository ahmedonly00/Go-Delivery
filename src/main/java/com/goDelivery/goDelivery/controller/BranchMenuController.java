package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dto.menu.MenuItemPartialUpdateDTO;
import com.goDelivery.goDelivery.dto.menu.MenuProgressiveResponseDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryResponseDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.dtos.menu.UpdateMenuItemRequest;
import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.service.BranchDelegationService;
import com.goDelivery.goDelivery.service.BranchMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
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
    private final BranchDelegationService delegationService;

    // ── Inheritance ───────────────────────────────────────────────────────────

    @PostMapping("/inherit")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Inherit restaurant menu",
               description = "Copy all menu categories and items from the restaurant to the branch")
    public ResponseEntity<List<MenuCategory>> inheritRestaurantMenu(
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Inheriting restaurant menu for branch {} by user {}", branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.inheritRestaurantMenu(branchId));
    }

    // ── Full menu views ───────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Get full branch menu",
               description = "Get all menu categories and items for the branch")
    public ResponseEntity<List<MenuCategory>> getBranchMenu(
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Fetching menu for branch {} by user {}", branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.getBranchMenu(branchId));
    }

    @GetMapping("/progressive")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Get menu progressively",
               description = "Get menu categories and items progressively for better performance")
    public ResponseEntity<MenuProgressiveResponseDTO> getMenuProgressive(
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryName,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Fetching menu progressively for branch {} by user {}", branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.getMenuProgressive(branchId, page, size, categoryName));
    }

    // ── Categories ────────────────────────────────────────────────────────────

    @GetMapping("/categories")
    @Operation(summary = "Get menu categories",
               description = "Get all menu categories for a branch")
    public ResponseEntity<List<MenuCategoryDTO>> getCategories(@PathVariable Long branchId) {
        log.info("Fetching menu categories for branch {}", branchId);
        return ResponseEntity.ok(branchMenuService.getBranchMenuCategories(branchId));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Create menu category",
               description = "Create a new menu category for the branch")
    public ResponseEntity<MenuCategoryResponseDTO> createCategory(
            @PathVariable Long branchId,
            @RequestBody @Valid MenuCategoryDTO categoryDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creating menu category '{}' for branch {} by user {}",
                categoryDTO.getCategoryName(), branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.createMenuCategory(branchId, categoryDTO));
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update menu category",
               description = "Update a menu category")
    public ResponseEntity<MenuCategoryResponseDTO> updateCategory(
            @PathVariable Long branchId,
            @PathVariable Long categoryId,
            @RequestBody @Valid MenuCategoryDTO categoryDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Updating menu category {} for branch {} by user {}",
                categoryId, branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.updateMenuCategory(branchId, categoryId, categoryDTO));
    }


    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Delete menu category",
               description = "Delete a menu category from the branch")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long branchId,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Deleting category {} from branch {} by user {}", categoryId, branchId, userDetails.getUsername());
        branchMenuService.deleteMenuCategory(branchId, categoryId);
        return ResponseEntity.noContent().build();
    }

    // ── Menu items ────────────────────────────────────────────────────────────

    @GetMapping("/categories/{categoryId}/items")
    @Operation(summary = "Get menu items",
               description = "Get all menu items in a category")
    public ResponseEntity<List<MenuItemResponse>> getMenuItems(
            @PathVariable Long branchId,
            @PathVariable Long categoryId) {

        log.info("Fetching menu items for category {} in branch {}", categoryId, branchId);
        return ResponseEntity.ok(branchMenuService.getBranchMenuItems(branchId, categoryId));
    }

    @PostMapping("/categories/{categoryId}/items")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Create menu item",
               description = "Create a new menu item in a category. Send as multipart/form-data with individual fields + optional 'imageFile' file part.")
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long categoryId,
            @ModelAttribute @Valid MenuItemRequest menuItemRequest,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creating menu item '{}' for category {} in branch {} by user {}",
                menuItemRequest.getMenuItemName(), categoryId, branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.createMenuItem(branchId, categoryId, menuItemRequest, imageFile));
    }

    @PutMapping("/items/{menuItemId}")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Update menu item",
               description = "Update an existing menu item. Send as multipart/form-data with individual fields + optional 'imageFile' file part.")
    public ResponseEntity<MenuItem> updateMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long menuItemId,
            @ModelAttribute UpdateMenuItemRequest updateRequest,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        log.info("Updating menu item {} for branch {} by user {}", menuItemId, branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.updateMenuItem(branchId, menuItemId, updateRequest, imageFile, request));
    }

    @PatchMapping("/items/{menuItemId}/autosave")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Auto-save menu item",
               description = "Partially update a menu item field for auto-save functionality")
    public ResponseEntity<MenuItem> autoSaveMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long menuItemId,
            @RequestBody MenuItemPartialUpdateDTO partialUpdate,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        log.info("Auto-saving menu item {} for branch {} by user {}", menuItemId, branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.partialUpdateMenuItem(branchId, menuItemId, partialUpdate, request));
    }

    @DeleteMapping("/items/{menuItemId}")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Delete menu item",
               description = "Delete a menu item from the branch")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long menuItemId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        log.info("Deleting menu item {} from branch {} by user {}", menuItemId, branchId, userDetails.getUsername());
        branchMenuService.deleteMenuItem(branchId, menuItemId, request);
        return ResponseEntity.noContent().build();
    }

    // ── Delegation menu endpoints ─────────────────────────────────────────────

    @GetMapping("/items")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Get branch menu items (flat list)",
               description = "Retrieve all menu items for the branch including restaurant menu with branch overrides")
    public ResponseEntity<List<MenuItemResponse>> getBranchMenuItems(
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting flat menu items for branch {} by user {}", branchId, userDetails.getUsername());
        return ResponseEntity.ok(delegationService.getBranchMenu(branchId));
    }

    @PostMapping(value = "/items/add", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Add menu item to branch",
               description = "Add a new menu item to the branch. Send as multipart/form-data with individual fields + optional 'imageFile' file part.")
    public ResponseEntity<MenuItemResponse> addBranchMenuItem(
            @PathVariable Long branchId,
            @ModelAttribute @Valid MenuItemRequest menuItemRequest,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Adding menu item '{}' to branch {} by user {}",
                menuItemRequest.getMenuItemName(), branchId, userDetails.getUsername());
        return new ResponseEntity<>(delegationService.addBranchMenuItem(branchId, menuItemRequest, imageFile),
                HttpStatus.CREATED);
    }

    @PutMapping("/items/{menuItemId}/update")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update branch menu item",
               description = "Update a menu item in the branch menu")
    public ResponseEntity<MenuItemResponse> updateBranchMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long menuItemId,
            @RequestBody @Valid MenuItemRequest menuItemRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Updating menu item {} for branch {} by user {}", menuItemId, branchId, userDetails.getUsername());
        return ResponseEntity.ok(delegationService.updateBranchMenuItem(branchId, menuItemId, menuItemRequest));
    }
}
