package com.goDelivery.goDelivery.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.service.BranchesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/branches")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Branches management")
@PreAuthorize("hasRole('RESTAURANT_ADMIN')")
public class BranchesController {
    
    private static final Logger log = LoggerFactory.getLogger(BranchesController.class);

    
    private final BranchesService branchesService;

    
    @PostMapping("/addBranch/{restaurantId}")
    public ResponseEntity<BranchesDTO> addBranchToRestaurant(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId,
            @Valid @RequestBody BranchesDTO branchDTO) {
        log.info("Adding branch details for branch {}", branchDTO.getBranchName());
        BranchesDTO createdBranch = branchesService.addBranchToRestaurant(restaurantId, branchDTO);
        return new ResponseEntity<>(createdBranch, HttpStatus.CREATED);
    }

    @GetMapping("/getBranches/{branchId}")
    public ResponseEntity<BranchesDTO> getBranchById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long branchId) {
        log.info("Getting branch details for branch {}", branchId);
        BranchesDTO branch = branchesService.getBranchById(branchId);
        return ResponseEntity.ok(branch);
    }

    @PutMapping("/updateBranch/{branchId}")
    public ResponseEntity<BranchesDTO> updateBranch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long branchId,
            @Valid @RequestBody BranchesDTO branchDTO) {
        log.info("Updating branch details for branch {}", branchId);
        BranchesDTO updatedBranch = branchesService.updateBranch(branchId, branchDTO);
        return ResponseEntity.ok(updatedBranch);
    }

    @GetMapping("/getBranchesByRestaurant/{restaurantId}")
    public ResponseEntity<List<BranchesDTO>> getBranchesByRestaurant(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long restaurantId) {
        log.info("Getting branch details for restaurant {}", restaurantId);
        List<BranchesDTO> branches = branchesService.getBranchesByRestaurant(restaurantId, userDetails.getUsername());
        return ResponseEntity.ok(branches);
    }

    @PutMapping("/{branchId}/status")
    @Operation(
        summary = "Toggle branch active status",
        description = "Enable or disable a restaurant branch. Only accessible by restaurant admins."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Branch status updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<BranchesDTO> toggleBranchStatus(
        @Parameter(description = "ID of the branch to update") @PathVariable Long branchId,
        @Parameter(description = "New active status") @RequestParam boolean isActive,
        @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating status for branch {} to {}", branchId, isActive ? "active" : "inactive");
        BranchesDTO updatedBranch = branchesService.toggleBranchStatus(branchId, isActive, userDetails.getUsername());
        return ResponseEntity.ok(updatedBranch);
    }
    
    @DeleteMapping("/deleteBranch/{branchId}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long branchId) {
        log.info("Deleting branch details for branch {}", branchId);
        branchesService.deleteBranch(branchId);
        return ResponseEntity.noContent().build();
    }
}
