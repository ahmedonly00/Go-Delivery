package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.Enum.BranchSetupStatus;
import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.model.OperatingHours;
import com.goDelivery.goDelivery.service.BranchSetupProgressService;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/branch-setup-progress")
@RequiredArgsConstructor
@Tag(name = "Branch Setup Progress", description = "Step-by-step branch setup management")
@CrossOrigin("*")
public class BranchSetupProgressController {

    private final BranchSetupProgressService branchSetupProgressService;

    @GetMapping("/{branchId}/status")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Get branch setup status",
        description = "Get the current setup status and progress of the branch"
    )
    public ResponseEntity<BranchesDTO> getSetupStatus(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching setup status for branch {} by user {}", 
                branchId, userDetails.getUsername());
        
        BranchesDTO branch = branchSetupProgressService.getBranchSetupStatus(branchId);
        return ResponseEntity.ok(branch);
    }

    @PutMapping("/{branchId}/status")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Update branch setup status",
        description = "Update the setup status of the branch"
    )
    public ResponseEntity<BranchesDTO> updateSetupStatus(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "New setup status") 
            @RequestParam BranchSetupStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating setup status to {} for branch {} by user {}", 
                status, branchId, userDetails.getUsername());
        
        BranchesDTO updatedBranch = branchSetupProgressService.updateBranchSetupStatus(branchId, status);
        return ResponseEntity.ok(updatedBranch);
    }

    @PutMapping("/{branchId}/location")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Add location details",
        description = "Update branch location with address and coordinates"
    )
    public ResponseEntity<BranchesDTO> addLocationDetails(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Address") 
            @RequestParam String address,
            @Parameter(description = "Latitude") 
            @RequestParam Float latitude,
            @Parameter(description = "Longitude") 
            @RequestParam Float longitude,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Adding location for branch {} by user {}", branchId, userDetails.getUsername());
        
        BranchesDTO updatedBranch = branchSetupProgressService.addLocationDetails(
                branchId, address, latitude, longitude);
        return ResponseEntity.ok(updatedBranch);
    }

    @PutMapping("/{branchId}/delivery-settings")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Configure delivery settings",
        description = "Set up delivery type, fees, radius, and preparation time"
    )
    public ResponseEntity<BranchesDTO> configureDeliverySettings(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Delivery type") 
            @RequestParam DeliveryType deliveryType,
            @Parameter(description = "Delivery fee") 
            @RequestParam(required = false) Float deliveryFee,
            @Parameter(description = "Delivery radius in km") 
            @RequestParam(required = false) Double deliveryRadius,
            @Parameter(description = "Minimum order amount") 
            @RequestParam(required = false) Float minimumOrderAmount,
            @Parameter(description = "Average preparation time in minutes") 
            @RequestParam(required = false) Integer averagePrepTime,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Configuring delivery settings for branch {} by user {}", 
                branchId, userDetails.getUsername());
        
        BranchesDTO updatedBranch = branchSetupProgressService.configureDeliverySettings(
                branchId, deliveryType, deliveryFee, deliveryRadius, 
                minimumOrderAmount, averagePrepTime);
        return ResponseEntity.ok(updatedBranch);
    }

    @PostMapping("/{branchId}/operating-hours")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Add operating hours",
        description = "Set the operating hours for the branch"
    )
    public ResponseEntity<BranchesDTO> addOperatingHours(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @RequestBody OperatingHours operatingHours,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Adding operating hours for branch {} by user {}", 
                branchId, userDetails.getUsername());
        
        BranchesDTO updatedBranch = branchSetupProgressService.addOperatingHours(
                branchId, operatingHours);
        return ResponseEntity.ok(updatedBranch);
    }

    @PutMapping("/{branchId}/branding")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Add branding elements",
        description = "Update branch logo and description"
    )
    public ResponseEntity<BranchesDTO> addBranding(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @Parameter(description = "Logo URL") 
            @RequestParam(required = false) String logoUrl,
            @Parameter(description = "Description") 
            @RequestParam(required = false) String description,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Adding branding for branch {} by user {}", branchId, userDetails.getUsername());
        
        BranchesDTO updatedBranch = branchSetupProgressService.addBranding(
                branchId, logoUrl, description);
        return ResponseEntity.ok(updatedBranch);
    }

    @PutMapping("/{branchId}/menu-setup/start")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Start menu setup",
        description = "Mark the beginning of menu setup process"
    )
    public ResponseEntity<BranchesDTO> startMenuSetup(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Starting menu setup for branch {} by user {}", 
                branchId, userDetails.getUsername());
        
        BranchesDTO updatedBranch = branchSetupProgressService.startMenuSetup(branchId);
        return ResponseEntity.ok(updatedBranch);
    }

    @PutMapping("/{branchId}/menu-setup/complete")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Complete menu setup",
        description = "Mark the menu setup as completed"
    )
    public ResponseEntity<BranchesDTO> completeMenuSetup(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Completing menu setup for branch {} by user {}", 
                branchId, userDetails.getUsername());
        
        BranchesDTO updatedBranch = branchSetupProgressService.completeMenuSetup(branchId);
        return ResponseEntity.ok(updatedBranch);
    }

    @PutMapping("/{branchId}/complete")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Complete branch setup",
        description = "Mark the entire branch setup as completed"
    )
    public ResponseEntity<BranchesDTO> completeBranchSetup(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Completing branch setup for branch {} by user {}", 
                branchId, userDetails.getUsername());
        
        BranchesDTO updatedBranch = branchSetupProgressService.completeBranchSetup(branchId);
        return ResponseEntity.ok(updatedBranch);
    }
}
