package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dto.branch.BranchSetupDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.service.BranchSetupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/branch-setup")
@RequiredArgsConstructor
@Tag(name = "Branch Setup", description = "Branch setup and configuration management")
@CrossOrigin("*")
public class BranchSetupController {

    private final BranchSetupService branchSetupService;

    @PostMapping("/{branchId}/complete")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Complete branch setup",
        description = "Complete the branch setup with location details and initial menu"
    )
    public ResponseEntity<BranchesDTO> completeBranchSetup(
            @PathVariable Long branchId,
            @RequestBody @Valid BranchSetupDTO setupDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Completing branch setup for branch {} by user {}", branchId, userDetails.getUsername());
        
        BranchesDTO updatedBranch = branchSetupService.completeBranchSetup(
                branchId, setupDTO);
        
        return ResponseEntity.ok(updatedBranch);
    }

    @GetMapping("/{branchId}/status")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Get branch setup status",
        description = "Get the current setup status and details of the branch"
    )
    public ResponseEntity<BranchSetupDTO> getBranchSetupStatus(
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching setup status for branch {} by user {}", branchId, userDetails.getUsername());
        
        BranchSetupDTO setupStatus = branchSetupService.getBranchSetupStatus(branchId);
        return ResponseEntity.ok(setupStatus);
    }

    @PutMapping("/{branchId}/location")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Update branch location",
        description = "Update the branch location coordinates and address"
    )
    public ResponseEntity<BranchesDTO> updateBranchLocation(
            @PathVariable Long branchId,
            @RequestParam Float latitude,
            @RequestParam Float longitude,
            @RequestParam String address,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating location for branch {} by user {}: {}, {}", 
                branchId, userDetails.getUsername(), latitude, longitude);
        
        BranchesDTO updatedBranch = branchSetupService.updateBranchLocation(
                branchId, latitude, longitude, address);
        
        return ResponseEntity.ok(updatedBranch);
    }

    @PostMapping("/{branchId}/bulk-categories")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Create multiple menu categories",
        description = "Create multiple menu categories in bulk"
    )
    public ResponseEntity<?> createBulkMenuCategories(
            @PathVariable Long branchId,
            @RequestParam("categoryNames") List<String> categoryNames,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating {} menu categories for branch {} by user {}", 
                categoryNames.size(), branchId, userDetails.getUsername());
        
        // Create a setup DTO with just the categories
        BranchSetupDTO setupDTO = new BranchSetupDTO();
        setupDTO.setInitialMenuCategories(categoryNames);
        
        BranchesDTO result = branchSetupService.completeBranchSetup(
                branchId, setupDTO);
        
        return ResponseEntity.ok(result);
    }
}
