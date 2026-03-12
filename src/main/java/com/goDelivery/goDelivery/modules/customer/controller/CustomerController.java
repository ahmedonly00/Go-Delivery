package com.goDelivery.goDelivery.modules.customer.controller;

import com.goDelivery.goDelivery.modules.restaurant.dto.BranchesDTO;
import com.goDelivery.goDelivery.modules.branch.model.BranchMenuCategory;
import com.goDelivery.goDelivery.modules.branch.service.BranchMenuService;
import com.goDelivery.goDelivery.modules.branch.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Customer", description = "Customer-facing endpoints for browsing branches, menus and placing orders")
public class CustomerController {

    private final BranchService branchService;
    private final BranchMenuService branchMenuService;

    /**
     * Step 1 — customer picks a restaurant from /api/restaurants/approved,
     * then calls this to see which branches are available.
     */
    @GetMapping("/restaurants/{restaurantId}/branches")
    @Operation(
            summary = "Get available branches for a restaurant",
            description = "Returns all approved and active branches for the given restaurant. " +
                    "Customer selects a branch before browsing the menu or placing an order.")
    public ResponseEntity<List<BranchesDTO>> getBranchesForRestaurant(
            @PathVariable Long restaurantId) {

        log.info("Customer fetching branches for restaurant {}", restaurantId);
        return ResponseEntity.ok(branchService.getApprovedActiveBranchesForRestaurant(restaurantId));
    }

    /**
     * Step 2 — customer views branch details (address, delivery info, hours, etc.).
     */
    @GetMapping("/branches/{branchId}")
    @Operation(
            summary = "Get branch details",
            description = "Returns details for a specific branch including address, delivery fee, " +
                    "operating hours and delivery availability.")
    public ResponseEntity<BranchesDTO> getBranchDetails(
            @PathVariable Long branchId) {

        log.info("Customer fetching details for branch {}", branchId);
        return ResponseEntity.ok(branchService.getBranchById(branchId));
    }

    /**
     * Step 3 — customer browses the branch menu (categories + items).
     * Uses POST /api/orders/createOrder to place the order.
     */
    @GetMapping("/branches/{branchId}/menu")
    @Operation(
            summary = "Get branch menu",
            description = "Returns the full menu for a branch organised by category.")
    public ResponseEntity<List<BranchMenuCategory>> getBranchMenu(
            @PathVariable Long branchId) {

        log.info("Customer fetching menu for branch {}", branchId);
        return ResponseEntity.ok(branchMenuService.getBranchMenu(branchId));
    }
}
