package com.goDelivery.goDelivery.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.goDelivery.goDelivery.dtos.auth.LoginRequest;
import com.goDelivery.goDelivery.dtos.auth.LoginResponse;
import com.goDelivery.goDelivery.dtos.restaurant.BranchUserDTO;
import com.goDelivery.goDelivery.auth.AuthenticationService;
import com.goDelivery.goDelivery.service.BranchUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/branch-users")
@RequiredArgsConstructor
@Tag(name = "Branch Users", description = "Branch user management")
@CrossOrigin("*")
@PreAuthorize("hasRole('RESTAURANT_ADMIN')")
public class BranchUserController {
    
    private static final Logger log = LoggerFactory.getLogger(BranchUserController.class);
    
    private final BranchUserService branchUserService;
    private final AuthenticationService authenticationService;

    @GetMapping("/my-branch")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    @Operation(
        summary = "Get current branch manager's branch",
        description = "Get the branch information for the currently logged in branch manager"
    )
    public ResponseEntity<BranchUserDTO> getMyBranch(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching branch for user: {}", userDetails.getUsername());
        
        BranchUserDTO branchUser = branchUserService.getCurrentUserBranch();
        return ResponseEntity.ok(branchUser);
    }

    @PostMapping("/branch/{branchId}")
    @Operation(
        summary = "Create branch user",
        description = "Create a new user for a specific branch"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Branch user created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Branch not found")
    })
    public ResponseEntity<BranchUserDTO> createBranchUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long branchId,
            @Valid @RequestBody BranchUserDTO branchUserDTO) {
        log.info("Creating branch user '{}' for branch {} by user '{}'", 
                branchUserDTO.getEmail(), branchId, userDetails.getUsername());
        BranchUserDTO createdUser = branchUserService.createBranchUser(branchId, branchUserDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/branch/{branchId}")
    @Operation(
        summary = "Get all branch users",
        description = "Retrieve all users belonging to a specific branch"
    )
    public ResponseEntity<List<BranchUserDTO>> getBranchUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long branchId) {
        log.info("Getting users for branch {} by user '{}'", branchId, userDetails.getUsername());
        List<BranchUserDTO> users = branchUserService.getBranchUsers(branchId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/branch/{branchId}/active")
    @Operation(
        summary = "Get active branch users",
        description = "Retrieve all active users belonging to a specific branch"
    )
    public ResponseEntity<List<BranchUserDTO>> getActiveBranchUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long branchId) {
        log.info("Getting active users for branch {} by user '{}'", branchId, userDetails.getUsername());
        List<BranchUserDTO> users = branchUserService.getActiveBranchUsers(branchId);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    @Operation(
        summary = "Update branch user",
        description = "Update details of a branch user"
    )
    public ResponseEntity<BranchUserDTO> updateBranchUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId,
            @Valid @RequestBody BranchUserDTO branchUserDTO) {
        log.info("Updating branch user {} by user '{}'", userId, userDetails.getUsername());
        BranchUserDTO updatedUser = branchUserService.updateBranchUser(userId, branchUserDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/status")
    @Operation(
        summary = "Toggle branch user status",
        description = "Enable or disable a branch user"
    )
    public ResponseEntity<Void> toggleBranchUserStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long userId,
            @Parameter(description = "New active status") @RequestParam boolean isActive) {
        log.info("Updating status for branch user {} to {} by user '{}'", 
                userId, isActive ? "active" : "inactive", userDetails.getUsername());
        branchUserService.toggleBranchUserStatus(userId, isActive);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    @Operation(
        summary = "Register a new branch manager",
        description = "Public endpoint to register a new branch manager"
    )
    public ResponseEntity<LoginResponse> registerBranchManager(
            @Valid @RequestBody BranchUserDTO branchUserDTO) {
        log.info("Registering new branch manager: {}", branchUserDTO.getEmail());
        
        // Create the branch user
        branchUserService.createBranchUser(
            branchUserDTO.getBranchId(), branchUserDTO);
        
        // Authenticate and return token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(branchUserDTO.getEmail());
        loginRequest.setPassword(branchUserDTO.getPassword());
        
        LoginResponse response = authenticationService.authenticate(loginRequest);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
