package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dto.menu.MenuItemPartialUpdateDTO;
import com.goDelivery.goDelivery.dto.menu.MenuProgressiveResponseDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryResponseDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.model.BranchMenuCategory;
import com.goDelivery.goDelivery.model.BranchMenuItem;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.service.BranchDelegationService;
import com.goDelivery.goDelivery.service.BranchMenuService;
import com.goDelivery.goDelivery.service.BranchService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Menu", description = "Branch menu management")
@CrossOrigin("*")
public class BranchMenuController {

    private final BranchMenuService branchMenuService;
    private final BranchDelegationService delegationService;
    private final BranchService branchService;

    // ── Inheritance ───────────────────────────────────────────────────────────

    @PostMapping("/{branchId}/menu/inherit")
    @Operation(summary = "Inherit restaurant menu", description = "Copy all menu categories and items from the restaurant to the branch")
    public ResponseEntity<List<BranchMenuCategory>> inheritRestaurantMenu(
            @PathVariable Long branchId) {

        log.info("Inheriting restaurant menu for branch {}", branchId);
        return ResponseEntity.ok(branchMenuService.inheritRestaurantMenu(branchId));
    }

    // ── Full menu views ───────────────────────────────────────────────────────

    @GetMapping("/{branchId}/menu")
    @Operation(summary = "Get full branch menu", description = "Get all menu categories and items for the branch. Accessible by branch managers and customers.")
    public ResponseEntity<List<BranchMenuCategory>> getBranchMenu(
            @PathVariable Long branchId) {

        log.info("Fetching menu for branch {}", branchId);
        return ResponseEntity.ok(branchMenuService.getBranchMenu(branchId));
    }

    @GetMapping("/{branchId}/menu/progressive")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Get menu progressively", description = "Get menu categories and items progressively for better performance")
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

    @GetMapping("/{branchId}/menu/categories")
    @Operation(summary = "Get menu categories", description = "Get all menu categories for a branch")
    public ResponseEntity<List<MenuCategoryDTO>> getCategories(@PathVariable Long branchId) {
        log.info("Fetching menu categories for branch {}", branchId);
        return ResponseEntity.ok(branchMenuService.getBranchMenuCategories(branchId));
    }

    @PostMapping("/{branchId}/menu/categories")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Create menu category", description = "Create a new menu category for the branch")
    public ResponseEntity<MenuCategoryResponseDTO> createCategory(
            @PathVariable Long branchId,
            @RequestBody @Valid MenuCategoryDTO categoryDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creating menu category '{}' for branch {} by user {}",
                categoryDTO.getCategoryName(), branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.createMenuCategory(branchId, categoryDTO));
    }

    @PutMapping("/{branchId}/menu/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update menu category", description = "Update a menu category")
    public ResponseEntity<MenuCategoryResponseDTO> updateCategory(
            @PathVariable Long branchId,
            @PathVariable Long categoryId,
            @RequestBody @Valid MenuCategoryDTO categoryDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Updating menu category {} for branch {} by user {}",
                categoryId, branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.updateMenuCategory(branchId, categoryId, categoryDTO));
    }

    @DeleteMapping("/{branchId}/menu/categories/{categoryId}")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Delete menu category", description = "Delete a menu category from the branch")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long branchId,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Deleting category {} from branch {} by user {}", categoryId, branchId, userDetails.getUsername());
        branchMenuService.deleteMenuCategory(branchId, categoryId);
        return ResponseEntity.noContent().build();
    }

    // ── Menu items ────────────────────────────────────────────────────────────

    @GetMapping("/{branchId}/menu/getMenuItems/{categoryId}")
    @Operation(summary = "Get menu items", description = "Get all menu items in a category")
    public ResponseEntity<List<MenuItemResponse>> getMenuItems(
            @PathVariable Long branchId,
            @PathVariable Long categoryId) {

        log.info("Fetching menu items for category {} in branch {}", categoryId, branchId);
        return ResponseEntity.ok(branchMenuService.getBranchMenuItems(branchId, categoryId));
    }

    @GetMapping("/{branchId}/menu/getBranchMenuItems")
    @Operation(summary = "Get branch menu items (flat list)", description = "Retrieve all menu items for the branch including restaurant menu with branch overrides")
    public ResponseEntity<List<MenuItemResponse>> getBranchMenuItems(
            @PathVariable Long branchId) {

        log.info("Getting flat menu items for branch {}", branchId);
        return ResponseEntity.ok(delegationService.getBranchMenu(branchId));
    }

    @PostMapping(value = "/{branchId}/menu/categories/{categoryId}/items/add", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Add menu item to branch", description = "Send as multipart/form-data. Fields: menuItemName (required), price (required), description, ingredients, isAvailable, preparationTime, imageFile (file).")
    public ResponseEntity<MenuItemResponse> addBranchMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long categoryId,
            @RequestParam String menuItemName,
            @RequestParam Float price,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String ingredients,
            @RequestParam(defaultValue = "true") boolean isAvailable,
            @RequestParam(required = false) Integer preparationTime,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Adding menu item '{}' to branch {} by user {}", menuItemName, branchId, userDetails.getUsername());
        MenuItemRequest request = MenuItemRequest.builder()
                .menuItemName(menuItemName)
                .price(price)
                .description(description)
                .ingredients(ingredients)
                .isAvailable(isAvailable)
                .preparationTime(preparationTime)
                .categoryId(categoryId)
                .build();
        return new ResponseEntity<>(
                delegationService.addBranchMenuItem(branchId, categoryId, request, imageFile),
                HttpStatus.CREATED);
    }

    @PutMapping(value = "/{branchId}/menu/items/{menuItemId}/update", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update branch menu item", description = "Send as multipart/form-data. All fields optional. Fields: menuItemName, price, description, ingredients, isAvailable, preparationTime, imageFile (file).")
    public ResponseEntity<MenuItemResponse> updateBranchMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long menuItemId,
            @RequestParam(required = false) String menuItemName,
            @RequestParam(required = false) Float price,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String ingredients,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) Integer preparationTime,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Updating menu item {} for branch {} by user {}", menuItemId, branchId, userDetails.getUsername());
        MenuItemRequest request = MenuItemRequest.builder()
                .menuItemName(menuItemName)
                .price(price)
                .description(description)
                .ingredients(ingredients)
                .isAvailable(isAvailable != null && isAvailable)
                .preparationTime(preparationTime)
                .build();
        return ResponseEntity.ok(delegationService.updateBranchMenuItem(branchId, menuItemId, request, imageFile));
    }

    @PatchMapping("/{branchId}/menu/items/{menuItemId}/autosave")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Auto-save menu item", description = "Partially update a menu item field for auto-save functionality")
    public ResponseEntity<BranchMenuItem> autoSaveMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long menuItemId,
            @RequestBody MenuItemPartialUpdateDTO partialUpdate,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        log.info("Auto-saving menu item {} for branch {} by user {}", menuItemId, branchId, userDetails.getUsername());
        return ResponseEntity.ok(branchMenuService.partialUpdateMenuItem(branchId, menuItemId, partialUpdate, request));
    }

    @DeleteMapping("/{branchId}/menu/items/{menuItemId}")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(summary = "Delete menu item", description = "Delete a menu item from the branch")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long menuItemId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        log.info("Deleting menu item {} from branch {} by user {}", menuItemId, branchId, userDetails.getUsername());
        branchMenuService.deleteMenuItem(branchId, menuItemId, request);
        return ResponseEntity.noContent().build();
    }

    // ── Customer-facing endpoints (no authentication required) ────────────────

    @GetMapping("/branches")
    @Operation(
            summary = "Get all available branches",
            description = "Returns all approved and active branches across every restaurant. Supports pagination via ?page=0&size=20&sort=branchId.")
    public ResponseEntity<Page<BranchesDTO>> getAllAvailableBranches(
            @ParameterObject @PageableDefault(size = 20, sort = "branchId") Pageable pageable) {

        log.info("Customer fetching all approved active branches - page {}", pageable.getPageNumber());
        return ResponseEntity.ok(branchService.getAllApprovedActiveBranchesPaged(pageable));
    }

    @GetMapping("/{branchId}/details")
    @Operation(
            summary = "Get branch details (customer)",
            description = "Returns branch details including address, delivery fee, and availability. " +
                    "Use GET /{branchId}/menu to browse the full menu, then POST /api/orders/createOrder to place an order.")
    public ResponseEntity<BranchesDTO> getBranchDetails(
            @PathVariable Long branchId) {

        log.info("Customer fetching details for branch {}", branchId);
        return ResponseEntity.ok(branchService.getBranchById(branchId));
    }

}
