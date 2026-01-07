package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.dtos.order.OrderRequest;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.restaurant.BranchUserDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.service.BranchDelegationService;
import com.goDelivery.goDelivery.service.BranchSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/branches/{branchId}")
@RequiredArgsConstructor
@Tag(name = "Branch Operations", description = "Branch-level operations for restaurant and branch managers")
@CrossOrigin("*")
public class BranchOperationsController {

    private final BranchDelegationService delegationService;
    private final BranchSecurityService branchSecurity;

    // Branch Information Operations
    @GetMapping("")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Get branch details",
        description = "Retrieve detailed information about a specific branch"
    )
    public ResponseEntity<BranchesDTO> getBranchDetails(
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting details for branch {} by user {}", branchId, userDetails.getUsername());
        BranchesDTO branch = delegationService.getBranchDetails(branchId);
        return ResponseEntity.ok(branch);
    }

    @PutMapping("")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Update branch details",
        description = "Update branch information"
    )
    public ResponseEntity<BranchesDTO> updateBranchDetails(
            @PathVariable Long branchId,
            @RequestBody @Valid BranchesDTO branchDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating branch {} by user {}", branchId, userDetails.getUsername());
        BranchesDTO updated = delegationService.updateBranchDetails(branchId, branchDTO);
        return ResponseEntity.ok(updated);
    }

    // Menu Operations
    @GetMapping("/menu")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Get branch menu",
        description = "Retrieve the menu for a specific branch (includes restaurant menu with branch overrides)"
    )
    public ResponseEntity<List<MenuItemResponse>> getBranchMenu(
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting menu for branch {} by user {}", branchId, userDetails.getUsername());
        List<MenuItemResponse> menu = delegationService.getBranchMenu(branchId);
        return ResponseEntity.ok(menu);
    }

    @PostMapping("/menu/items")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Add menu item to branch",
        description = "Add a new menu item specific to this branch"
    )
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable Long branchId,
            @RequestBody @Valid MenuItemRequest menuItemRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Adding menu item to branch {} by user {}", branchId, userDetails.getUsername());
        MenuItemResponse created = delegationService.addBranchMenuItem(branchId, menuItemRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/menu/items/{menuItemId}")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Update menu item",
        description = "Update a menu item in the branch menu"
    )
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long branchId,
            @PathVariable Long menuItemId,
            @RequestBody @Valid MenuItemRequest menuItemRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating menu item {} for branch {} by user {}", menuItemId, branchId, userDetails.getUsername());
        MenuItemResponse updated = delegationService.updateBranchMenuItem(branchId, menuItemId, menuItemRequest);
        return ResponseEntity.ok(updated);
    }

    // Order Operations
    @PostMapping("/orders")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Create order for branch",
        description = "Create a new order for this specific branch"
    )
    public ResponseEntity<List<OrderResponse>> createOrder(
            @PathVariable Long branchId,
            @RequestBody @Valid OrderRequest orderRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating order for branch {} by user {}", branchId, userDetails.getUsername());
        List<OrderResponse> orders = delegationService.createOrderForBranch(orderRequest, branchId);
        return new ResponseEntity<>(orders, HttpStatus.CREATED);
    }

    @GetMapping("/orders")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Get branch orders",
        description = "Retrieve orders for this branch, optionally filtered by status"
    )
    public ResponseEntity<List<OrderResponse>> getBranchOrders(
            @PathVariable Long branchId,
            @Parameter(description = "Filter by order status") @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting orders for branch {} with status {} by user {}", branchId, status, userDetails.getUsername());
        List<OrderResponse> orders = delegationService.getBranchOrders(branchId, status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Get specific order",
        description = "Retrieve details of a specific order from this branch"
    )
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long branchId,
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting order {} for branch {} by user {}", orderId, branchId, userDetails.getUsername());
        // Implementation would depend on your existing order service
        return ResponseEntity.ok().build();
    }

    // User Management (Restaurant Admin Only)
    @PostMapping("/users")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)")
    @Operation(
        summary = "Create branch user",
        description = "Create a new user for this branch (Restaurant Admin only)"
    )
    public ResponseEntity<BranchUserDTO> createBranchUser(
            @PathVariable Long branchId,
            @RequestBody @Valid BranchUserDTO userDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating branch user for branch {} by restaurant admin {}", branchId, userDetails.getUsername());
        BranchUserDTO created = delegationService.createBranchUser(branchId, userDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Get branch users",
        description = "Retrieve all users belonging to this branch"
    )
    public ResponseEntity<List<BranchUserDTO>> getBranchUsers(
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting users for branch {} by user {}", branchId, userDetails.getUsername());
        List<BranchUserDTO> users = delegationService.getBranchUsers(branchId);
        return ResponseEntity.ok(users);
    }

    // Analytics and Reports (Restaurant Admin Only)
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)")
    @Operation(
        summary = "Get branch analytics",
        description = "Retrieve analytics and reports for this branch (Restaurant Admin only)"
    )
    public ResponseEntity<?> getBranchAnalytics(
            @PathVariable Long branchId,
            @Parameter(description = "Report type") @RequestParam(required = false) String reportType,
            @Parameter(description = "Start date") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date") @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting analytics for branch {} by restaurant admin {}", branchId, userDetails.getUsername());
        // Implementation would depend on your existing analytics service
        return ResponseEntity.ok().build();
    }

    // Settings and Configuration
    @GetMapping("/settings")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Get branch settings",
        description = "Retrieve configuration settings for this branch"
    )
    public ResponseEntity<?> getBranchSettings(
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Getting settings for branch {} by user {}", branchId, userDetails.getUsername());
        // Implementation would depend on your settings structure
        return ResponseEntity.ok().build();
    }

    @PutMapping("/settings")
    @PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
                  "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
    @Operation(
        summary = "Update branch settings",
        description = "Update configuration settings for this branch"
    )
    public ResponseEntity<?> updateBranchSettings(
            @PathVariable Long branchId,
            @RequestBody Object settings,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating settings for branch {} by user {}", branchId, userDetails.getUsername());
        // Implementation would depend on your settings structure
        return ResponseEntity.ok().build();
    }
}
