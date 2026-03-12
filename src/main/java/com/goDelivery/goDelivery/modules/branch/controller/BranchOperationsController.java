package com.goDelivery.goDelivery.modules.branch.controller;

import com.goDelivery.goDelivery.modules.restaurant.dto.BranchSettingsDTO;
import com.goDelivery.goDelivery.modules.restaurant.dto.BranchUserDTO;
import com.goDelivery.goDelivery.modules.restaurant.dto.BranchesDTO;
import com.goDelivery.goDelivery.service.BranchDelegationService;
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
@RequestMapping("/api/v1/branches/operations")
@RequiredArgsConstructor
@Tag(name = "Branch Operations", description = "Branch-level operations for branch managers")
@CrossOrigin("*")
public class BranchOperationsController {

        private final BranchDelegationService delegationService;

        // Branch Information Operations
        @GetMapping("/getBranchDetails")
        @PreAuthorize("hasRole('BRANCH_MANAGER')")
        @Operation(summary = "Get branch details", description = "Retrieve detailed information about a specific branch")
        public ResponseEntity<BranchesDTO> getBranchDetails(
                        @PathVariable Long branchId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                log.info("Getting details for branch {} by user {}", branchId, userDetails.getUsername());
                BranchesDTO branch = delegationService.getBranchDetails(branchId);
                return ResponseEntity.ok(branch);
        }

        @PutMapping("/updateBranchDetails")
        @PreAuthorize("hasRole('BRANCH_MANAGER')")
        @Operation(summary = "Update branch details", description = "Update branch information")
        public ResponseEntity<BranchesDTO> updateBranchDetails(
                        @PathVariable Long branchId,
                        @RequestBody @Valid BranchesDTO branchDTO,
                        @AuthenticationPrincipal UserDetails userDetails) {

                log.info("Updating branch {} by user {}", branchId, userDetails.getUsername());
                BranchesDTO updated = delegationService.updateBranchDetails(branchId, branchDTO);
                return ResponseEntity.ok(updated);
        }

        // User Management (Restaurant Admin and Branch Manager)
        @PostMapping("/CreateUser/{branchId}")
        @PreAuthorize("hasRole('BRANCH_MANAGER')")
        @Operation(summary = "Create branch user", description = "Create a new user for this branch (Restaurant Admin only)")
        public ResponseEntity<BranchUserDTO> createBranchUser(
                        @PathVariable Long branchId,
                        @RequestBody @Valid BranchUserDTO userDTO,
                        @AuthenticationPrincipal UserDetails userDetails) {

                log.info("Creating branch user for branch {} by branch manager {}", branchId,
                                userDetails.getUsername());
                BranchUserDTO created = delegationService.createBranchUser(branchId, userDTO);
                return new ResponseEntity<>(created, HttpStatus.CREATED);
        }

        @GetMapping("/getBranchUsers/{branchId}")
        @PreAuthorize("hasRole('BRANCH_MANAGER')")
        @Operation(summary = "Get branch users", description = "Retrieve all users belonging to this branch")
        public ResponseEntity<List<BranchUserDTO>> getBranchUsers(
                        @PathVariable Long branchId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                log.info("Getting users for branch {} by branch manager {}", branchId, userDetails.getUsername());
                List<BranchUserDTO> users = delegationService.getBranchUsers(branchId);
                return ResponseEntity.ok(users);
        }

        // Analytics and Reports (Restaurant Admin and Branch Manager)
        @GetMapping("/getBranchAnalytics/{branchId}")
        @PreAuthorize("hasRole('BRANCH_MANAGER')")
        @Operation(summary = "Get branch analytics", description = "Retrieve analytics and reports for this branch")
        public ResponseEntity<Object> getBranchAnalytics(
                        @PathVariable Long branchId,
                        @Parameter(description = "Report type: SALES, CUSTOMER_TRENDS, HISTORY") @RequestParam(required = false, defaultValue = "SALES") String reportType,
                        @Parameter(description = "Year (e.g. 2025)") @RequestParam(required = false) Integer year,
                        @Parameter(description = "Month (1-12)") @RequestParam(required = false) Integer month,
                        @Parameter(description = "ISO week number (1-53)") @RequestParam(required = false) Integer week,
                        @AuthenticationPrincipal UserDetails userDetails) {

                log.info("Getting analytics for branch {} by branch manager {}", branchId, userDetails.getUsername());
                Object analytics = delegationService.getBranchAnalytics(branchId, reportType, year, month, week);
                return ResponseEntity.ok(analytics);
        }

        // Settings and Configuration
        @GetMapping("/getBranchSettings/{branchId}")
        @PreAuthorize("hasRole('BRANCH_MANAGER')")
        @Operation(summary = "Get branch settings", description = "Retrieve configuration settings for this branch")
        public ResponseEntity<?> getBranchSettings(
                        @PathVariable Long branchId,
                        @AuthenticationPrincipal UserDetails userDetails) {

                log.info("Getting settings for branch {} by user {}", branchId, userDetails.getUsername());
                Object settings = delegationService.getBranchSettings(branchId);
                return ResponseEntity.ok(settings);
        }

        @PutMapping("/updateBranchSettings/{branchId}")
        @PreAuthorize("hasRole('BRANCH_MANAGER')")
        @Operation(summary = "Update branch settings", description = "Update configuration settings for this branch")
        public ResponseEntity<?> updateBranchSettings(
                        @PathVariable Long branchId,
                        @RequestBody BranchSettingsDTO settings,
                        @AuthenticationPrincipal UserDetails userDetails) {

                log.info("Updating settings for branch {} by branch manager {}", branchId, userDetails.getUsername());
                Object updatedSettings = delegationService.updateBranchSettings(branchId, settings);
                return ResponseEntity.ok(updatedSettings);
        }
}
