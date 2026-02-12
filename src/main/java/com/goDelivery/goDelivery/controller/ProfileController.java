package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.profile.PasswordChangeRequest;
import com.goDelivery.goDelivery.dtos.profile.ProfileResponse;
import com.goDelivery.goDelivery.dtos.profile.ProfileUpdateRequest;
import com.goDelivery.goDelivery.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Profile", description = "User profile management for all user types")
@SecurityRequirement(name = "Bearer Authentication")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's profile", description = "Returns the profile of the currently authenticated user (works for all user types)")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        log.info("Getting profile for current user");
        ProfileResponse profile = profileService.getMyProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user's profile", description = "Updates the profile of the currently authenticated user. Only provided fields will be updated.")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Updating profile for current user");
        ProfileResponse updatedProfile = profileService.updateMyProfile(request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Changes the password for the currently authenticated user. Requires current password for verification.")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("Changing password for current user");

        try {
            profileService.changePassword(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password changed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error changing password", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
