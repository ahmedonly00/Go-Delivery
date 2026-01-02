package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dto.branch.BranchRegistrationDTO;
import com.goDelivery.goDelivery.dtos.restaurant.BranchesDTO;
import com.goDelivery.goDelivery.service.BranchRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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


@Slf4j
@RestController
@RequestMapping("/api/v1/branch-registration")
@RequiredArgsConstructor
@Tag(name = "Branch Registration", description = "Branch registration and approval management")
@CrossOrigin("*")
public class BranchRegistrationController {
    
    private final BranchRegistrationService branchRegistrationService;

    @PostMapping("/register")
    @Operation(
        summary = "Register a new branch",
        description = "Submit a new branch registration with documents and manager details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Branch registration submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data"),
        @ApiResponse(responseCode = "409", description = "Branch already exists")
    })
    public ResponseEntity<BranchesDTO> registerBranch(
            @RequestParam("branchData") String branchDataJson,
            @RequestParam("businessDocument") MultipartFile businessDocument,
            @RequestParam("operatingLicense") MultipartFile operatingLicense) {
        
        // Parse JSON data
        BranchRegistrationDTO registrationDTO = parseBranchData(branchDataJson);
        
        log.info("Received branch registration request for: {}", registrationDTO.getBranchName());
        
        BranchesDTO registeredBranch = branchRegistrationService.registerBranch(
                registrationDTO, businessDocument, operatingLicense);
        
        return new ResponseEntity<>(registeredBranch, HttpStatus.CREATED);
    }

    @PutMapping("/{branchId}/approve")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(
        summary = "Approve a branch registration",
        description = "Approve a pending branch registration"
    )
    public ResponseEntity<BranchesDTO> approveBranch(
            @PathVariable Long branchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Approving branch {} by user {}", branchId, userDetails.getUsername());
        
        BranchesDTO approvedBranch = branchRegistrationService.approveBranch(
                branchId, userDetails.getUsername());
        
        return ResponseEntity.ok(approvedBranch);
    }

    @PutMapping("/{branchId}/reject")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(
        summary = "Reject a branch registration",
        description = "Reject a pending branch registration with reason"
    )
    public ResponseEntity<BranchesDTO> rejectBranch(
            @PathVariable Long branchId,
            @RequestParam String rejectionReason,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Rejecting branch {} by user {} - Reason: {}", 
                branchId, userDetails.getUsername(), rejectionReason);
        
        BranchesDTO rejectedBranch = branchRegistrationService.rejectBranch(
                branchId, rejectionReason, userDetails.getUsername());
        
        return ResponseEntity.ok(rejectedBranch);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    @Operation(
        summary = "Get pending branch registrations",
        description = "Retrieve all pending branch registrations for the restaurant"
    )
    public ResponseEntity<?> getPendingBranches(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching pending branches for user: {}", userDetails.getUsername());
        
        var pendingBranches = branchRegistrationService.getPendingBranches();
        return ResponseEntity.ok(pendingBranches);
    }
    
    private BranchRegistrationDTO parseBranchData(String json) {
        // Use Jackson or similar to parse JSON
        // For now, returning a placeholder
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, BranchRegistrationDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse branch data", e);
        }
    }
}
