package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dto.branch.BranchManagerSetupDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.service.BranchSetupService;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/branch-setup")
@RequiredArgsConstructor
@Tag(name = "Branch Setup", description = "Branch setup and configuration management")
@CrossOrigin("*")
public class BranchSetupController {

    private final BranchSetupService branchSetupService;

    // ==================== SINGLE API BRANCH MANAGER SETUP ====================

    @PostMapping("/{branchId}/manager-setup")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Complete branch manager setup (Single API)",
        description = "Branch manager completes all remaining setup in a single API call. " +
                      "Only provide fields that need to be set - null fields are ignored. " +
                      "This allows the branch manager to fill in only what the restaurant admin didn't set."
    )
    public ResponseEntity<BranchesDTO> completeBranchManagerSetup(
            @PathVariable Long branchId,
            @RequestPart("setup") @Valid BranchManagerSetupDTO setupDTO,
            @RequestPart(value = "commercialRegistrationFile", required = false) MultipartFile commercialRegistrationFile,
            @RequestPart(value = "taxIdentificationFile", required = false) MultipartFile taxIdentificationFile,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Branch manager completing setup for branch {} by user {}", 
                branchId, userDetails.getUsername());

        setupDTO.setCommercialRegistrationFile(commercialRegistrationFile);
        setupDTO.setTaxIdentificationFile(taxIdentificationFile);

        BranchesDTO result = branchSetupService.completeBranchManagerSetup(branchId, setupDTO);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{branchId}/manager-setup")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Get current branch setup status",
        description = "Returns the current state of the branch so the frontend can show which fields are already set " +
                      "and which ones the branch manager still needs to fill in."
    )
    public ResponseEntity<BranchManagerSetupDTO> getBranchManagerSetupStatus(
            @Parameter(description = "Branch ID") 
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching branch manager setup status for branch {} by user {}", 
                branchId, userDetails.getUsername());
        
        BranchManagerSetupDTO status = branchSetupService.getBranchManagerSetupStatus(branchId);
        
        return ResponseEntity.ok(status);
    }
}
