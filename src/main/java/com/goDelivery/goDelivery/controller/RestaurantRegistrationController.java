package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.*;
import com.goDelivery.goDelivery.service.RestaurantRegistrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant/registration")
@RequiredArgsConstructor
public class RestaurantRegistrationController {

    private final RestaurantRegistrationService registrationService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<RestaurantAdminResponseDTO> registerRestaurant(
            @Valid @RequestBody RestaurantAdminRegistrationDTO registrationDTO) {
        return new ResponseEntity<>(
                registrationService.registerRestaurantAdmin(registrationDTO),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean isVerified = emailVerificationService.verifyEmail(token);
        if (isVerified) {
            return ResponseEntity.ok("Email verified successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired verification token");
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String email) {
        emailVerificationService.resendVerificationEmail(email);
        return ResponseEntity.ok("Verification email resent successfully");
    }

    @PostMapping("/basic-info")
    public ResponseEntity<RestaurantResponseDTO> saveBasicInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RestaurantBasicInfoDTO basicInfoDTO) {
        return ResponseEntity.ok(registrationService.saveBasicInfo(userDetails.getUsername(), basicInfoDTO));
    }

    @PostMapping("/location")
    public ResponseEntity<RestaurantResponseDTO> saveLocation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RestaurantLocationDTO locationDTO) {
        return ResponseEntity.ok(registrationService.saveLocation(userDetails.getUsername(), locationDTO));
    }

    @PostMapping("/branding")
    public ResponseEntity<RestaurantResponseDTO> saveBranding(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RestaurantBrandingDTO brandingDTO) {
        return ResponseEntity.ok(registrationService.saveBranding(userDetails.getUsername(), brandingDTO));
    }

    @PostMapping("/settings")
    public ResponseEntity<RestaurantResponseDTO> saveSettings(
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
    public ResponseEntity<RestaurantResponseDTO> completeSetup(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(registrationService.completeSetup(userDetails.getUsername()));
    }
        }
        throw new IllegalArgumentException("Invalid Authorization header");
    }
}
