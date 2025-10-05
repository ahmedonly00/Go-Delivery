package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.*;
import com.goDelivery.goDelivery.service.RestaurantRegistrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant/registration")
@RequiredArgsConstructor
public class RestaurantRegistrationController {

    private final RestaurantRegistrationService registrationService;

    @PostMapping("/registerAdmin")
    public ResponseEntity<RestaurantAdminResponseDTO> registerAdmin(
            @Valid @RequestBody RestaurantAdminRegistrationDTO registrationDTO) {
        return new ResponseEntity<>(
                registrationService.registerRestaurantAdmin(registrationDTO),
                HttpStatus.CREATED
        );
    }
    
    @PostMapping("/restaurant")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public ResponseEntity<RestaurantDTO> createRestaurant(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RestaurantDTO restaurantDTO) {
        return new ResponseEntity<>(
                registrationService.createRestaurant(userDetails.getUsername(), restaurantDTO),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/basic-info")
    public ResponseEntity<RestaurantDTO> saveBasicInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RestaurantBasicInfoDTO basicInfoDTO) {
        return ResponseEntity.ok(registrationService.saveBasicInfo(userDetails.getUsername(), basicInfoDTO));
    }

    @PostMapping("/settings")
    public ResponseEntity<RestaurantDTO> saveSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RestaurantSettingsDTO settingsDTO) {
        return ResponseEntity.ok(registrationService.saveSettings(userDetails.getUsername(), settingsDTO));
    }

    @GetMapping("/setup-progress")
    public ResponseEntity<RestaurantSetupProgressDTO> getSetupProgress(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(registrationService.getSetupProgress(userDetails.getUsername()));
    }

    @PostMapping("/complete-setup")
    public ResponseEntity<RestaurantDTO> completeSetup(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(registrationService.completeSetup(userDetails.getUsername()));
    }
    
}
