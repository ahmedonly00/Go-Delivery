package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dto.branch.BranchCreationDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.service.BranchService;
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
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "Comprehensive branch creation and management")
@CrossOrigin("*")
public class BranchController {

    private final BranchService branchService;

    @PostMapping("/create/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(
        summary = "Create a new branch",
        description = "Create a basic branch with essential information"
    )
    public ResponseEntity<BranchesDTO> createBranch(
            @Parameter(description = "Restaurant ID") 
            @PathVariable Long restaurantId,
            @RequestBody @Valid BranchCreationDTO creationDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating new branch for restaurant {} by user {}", restaurantId, userDetails.getUsername());
        
        BranchesDTO createdBranch = branchService.createBranch(restaurantId, creationDTO);
        
        return ResponseEntity.ok(createdBranch);
    }

    @PutMapping("/update/{branchId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Update branch details",
        description = "Update branch information including logo"
    )
    public ResponseEntity<BranchesDTO> updateBranch(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @RequestPart("branchData") @Valid BranchCreationDTO updateDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating branch {} by user {}", branchId, userDetails.getUsername());
        
        BranchesDTO updatedBranch = branchService.updateBranchComprehensive(
                branchId, updateDTO, logoFile);
        
        return ResponseEntity.ok(updatedBranch);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(
        summary = "Get all branches for a restaurant",
        description = "Retrieve all branches belonging to a specific restaurant"
    )
    public ResponseEntity<List<BranchesDTO>> getRestaurantBranches(
            @Parameter(description = "Restaurant ID") 
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching branches for restaurant {} by user {}", restaurantId, userDetails.getUsername());
        
        List<BranchesDTO> branches = branchService.getBranchesByRestaurant(restaurantId);
        
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/{branchId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'BRANCH_MANAGER')")
    @Operation(
        summary = "Get branch details",
        description = "Get detailed information about a specific branch"
    )
    public ResponseEntity<BranchesDTO> getBranch(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching branch {} by user {}", branchId, userDetails.getUsername());
        
        BranchesDTO branch = branchService.getBranchById(branchId);
        
        return ResponseEntity.ok(branch);
    }

    @PostMapping("/{branchId}/activate")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(
        summary = "Activate a branch",
        description = "Activate a branch that has been approved"
    )
    public ResponseEntity<Void> activateBranch(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Activating branch {} by user {}", branchId, userDetails.getUsername());
        
        branchService.activateBranch(branchId);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{branchId}/deactivate")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(
        summary = "Deactivate a branch",
        description = "Deactivate a branch temporarily"
    )
    public ResponseEntity<Void> deactivateBranch(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Deactivating branch {} by user {}", branchId, userDetails.getUsername());
        
        branchService.deactivateBranch(branchId);
        
        return ResponseEntity.ok().build();
    }
}
