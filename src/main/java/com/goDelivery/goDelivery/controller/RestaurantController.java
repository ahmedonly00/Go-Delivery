package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.*;
import com.goDelivery.goDelivery.service.RestaurantService;
import com.goDelivery.goDelivery.service.FileStorageService;
import java.util.HashMap;
import java.util.Map;
import com.goDelivery.goDelivery.service.RestaurantRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantRegistrationService registrationService;
    private final FileStorageService fileStorageService;


    @PostMapping("/registerAdmin")
    public ResponseEntity<RestaurantAdminResponseDTO> registerAdmin(
            @Valid @RequestBody RestaurantAdminRegistrationDTO registrationDTO) {
        return new ResponseEntity<>(
                registrationService.registerRestaurantAdmin(registrationDTO),
                HttpStatus.CREATED
        );
    }

    @PostMapping(value = "/registerRestaurant", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public ResponseEntity<?> registerRestaurant(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("restaurant") @Valid RestaurantDTO restaurantDTO,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestPart(value = "commercialRegistrationCertificate", required = false) MultipartFile commercialRegistrationCertificate,
            @RequestPart(value = "taxIdentificationDocument", required = false) MultipartFile taxIdentificationDocument,
            @RequestPart(value = "businessOperatingLicense", required = false) MultipartFile businessOperatingLicense) {
        try {
            // Store the logo file
            if (logoFile != null && !logoFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(logoFile, "restaurants/temp/logo");
                String fullUrl = "/api/files/" + filePath.replace("\\", "/");
                restaurantDTO.setLogoUrl(fullUrl);
            }
            
            // Store Commercial Registration Certificate
            if (commercialRegistrationCertificate != null && !commercialRegistrationCertificate.isEmpty()) {
                String filePath = fileStorageService.storeFile(commercialRegistrationCertificate, 
                    "restaurants/temp/documents/commercial-registration");
                String fullUrl = "/api/files/" + filePath.replace("\\", "/");
                restaurantDTO.setCommercialRegistrationCertificateUrl(fullUrl);
            }
            
            // Store Tax Identification Document (NUIT PDF)
            if (taxIdentificationDocument != null && !taxIdentificationDocument.isEmpty()) {
                String filePath = fileStorageService.storeFile(taxIdentificationDocument, 
                    "restaurants/temp/documents/tax-identification");
                String fullUrl = "/api/files/" + filePath.replace("\\", "/");
                restaurantDTO.setTaxIdentificationDocumentUrl(fullUrl);
            }
            
            // Store Business Operating License
            if (businessOperatingLicense != null && !businessOperatingLicense.isEmpty()) {
                String filePath = fileStorageService.storeFile(businessOperatingLicense, 
                    "restaurants/temp/documents/operating-license");
                String fullUrl = "/api/files/" + filePath.replace("\\", "/");
                restaurantDTO.setBusinessOperatingLicenseUrl(fullUrl);
            }
            
            RestaurantDTO createdRestaurant = registrationService.completeRestaurantRegistration(
                userDetails.getUsername(), 
                restaurantDTO
            );
            
            // Create success response with message
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Restaurant registered successfully!");
            successResponse.put("restaurant", createdRestaurant);
            
            return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to register restaurant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
    
    @PutMapping(value = "/{restaurantId}/operating-hours")
    public ResponseEntity<RestaurantDTO> updateOperatingHours(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId,
            @Valid @RequestBody UpdateOperatingHoursRequest request) {
        RestaurantDTO updatedRestaurant = restaurantService.updateOperatingHours(restaurantId, request);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @GetMapping(value = "/getRestaurantById/{restaurantId}")
    public ResponseEntity<RestaurantDTO> getRestaurantById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId) {
        
        RestaurantDTO restaurant = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant);
    }

    @PutMapping(value = "/updateRestaurant/{restaurantId}")
    public ResponseEntity<RestaurantDTO> updateRestaurant(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantDTO restaurantDTO) {
        RestaurantDTO updatedRestaurant = restaurantService.updateRestaurant(restaurantId, restaurantDTO);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @GetMapping(value = "/getRestaurantsByLocation/{location}")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByLocation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String location) {
        List<RestaurantDTO> restaurants = restaurantService.getRestaurantsByLocation(location);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping(value = "/getRestaurantsByCuisineType/{cuisineType}")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByCuisineType(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String cuisineType) {
        List<RestaurantDTO> restaurants = restaurantService.getRestaurantsByCuisineType(cuisineType);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping(value = "/searchRestaurants")
    public ResponseEntity<List<RestaurantDTO>> searchRestaurants(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RestaurantSearchRequest searchRequest) {
        List<RestaurantDTO> restaurants = restaurantService.searchRestaurants(searchRequest);
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping(value = "/getAllActiveRestaurants")
    public ResponseEntity<List<RestaurantDTO>> getAllActiveRestaurants(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<RestaurantDTO> restaurants = restaurantService.getAllActiveRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    // Super Admin: Get pending restaurants for review (with documents)
    @GetMapping(value = "/pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<com.goDelivery.goDelivery.dtos.restaurant.RestaurantReviewDTO>> getPendingRestaurants(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<com.goDelivery.goDelivery.dtos.restaurant.RestaurantReviewDTO> pendingRestaurants = 
            restaurantService.getPendingRestaurantsForReview();
        return ResponseEntity.ok(pendingRestaurants);
    }

    // Super Admin: Get specific restaurant details for review (with all documents)
    @GetMapping(value = "/{restaurantId}/review-details")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<com.goDelivery.goDelivery.dtos.restaurant.RestaurantReviewDTO> getRestaurantForReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId) {
        com.goDelivery.goDelivery.dtos.restaurant.RestaurantReviewDTO restaurant = 
            restaurantService.getRestaurantForReview(restaurantId);
        return ResponseEntity.ok(restaurant);
    }

    // Super Admin: Get restaurants by approval status
    @GetMapping(value = "/byApprovalStatus/{status}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByApprovalStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable com.goDelivery.goDelivery.Enum.ApprovalStatus status) {
        List<RestaurantDTO> restaurants = restaurantService.getRestaurantsByApprovalStatus(status);
        return ResponseEntity.ok(restaurants);
    }

    // Super Admin: Approve or reject restaurant
    @PostMapping(value = "/{restaurantId}/review")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> reviewRestaurant(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId,
            @Valid @RequestBody com.goDelivery.goDelivery.dtos.restaurant.RestaurantApprovalRequest request) {
        try {
            RestaurantDTO reviewedRestaurant;
            String message;
            
            if (request.getApproved()) {
                // Approve restaurant
                reviewedRestaurant = restaurantService.approveRestaurant(restaurantId, userDetails.getUsername());
                message = "Restaurant approved successfully! Notification email has been sent to the restaurant admin.";
                
            } else {
                // Reject restaurant
                if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Rejection reason is required when rejecting a restaurant");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                
                reviewedRestaurant = restaurantService.rejectRestaurant(
                    restaurantId, 
                    request.getRejectionReason(), 
                    userDetails.getUsername()
                );
                message = "Restaurant rejected. Notification email has been sent to the restaurant admin.";
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", message);
            response.put("restaurant", reviewedRestaurant);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to review restaurant: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Customer: Get only approved restaurants
    @GetMapping(value = "/approved")
    public ResponseEntity<List<RestaurantDTO>> getApprovedRestaurants() {
        List<RestaurantDTO> approvedRestaurants = restaurantService.getApprovedRestaurants();
        return ResponseEntity.ok(approvedRestaurants);
    }

    @PostMapping(value = "/{restaurantId}/uploadBusinessDocuments")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public ResponseEntity<?> uploadBusinessDocuments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId,
            @RequestParam(value = "commercialRegistrationCertificate", required = false) MultipartFile commercialRegistrationCertificate,
            @RequestParam(value = "taxIdentificationNumber", required = false) String taxIdentificationNumber,
            @RequestParam(value = "taxIdentificationDocument", required = false) MultipartFile taxIdentificationDocument,
            @RequestParam(value = "businessOperatingLicense", required = false) MultipartFile businessOperatingLicense) {
        try {
            Map<String, String> documentUrls = new HashMap<>();
            
            // Upload Commercial Registration Certificate
            if (commercialRegistrationCertificate != null && !commercialRegistrationCertificate.isEmpty()) {
                String filePath = fileStorageService.storeFile(commercialRegistrationCertificate, 
                    "restaurants/" + restaurantId + "/documents/commercial-registration");
                String fullUrl = "/api/files/" + filePath.replace("\\", "/");
                restaurantService.updateCommercialRegistrationCertificate(restaurantId, fullUrl);
                documentUrls.put("commercialRegistrationCertificateUrl", fullUrl);
            }
            
            // Update Tax Identification Number (text)
            if (taxIdentificationNumber != null && !taxIdentificationNumber.trim().isEmpty()) {
                restaurantService.updateTaxIdentificationNumber(restaurantId, taxIdentificationNumber);
                documentUrls.put("taxIdentificationNumber", taxIdentificationNumber);
            }
            
            // Upload Tax Identification Document (NUIT PDF)
            if (taxIdentificationDocument != null && !taxIdentificationDocument.isEmpty()) {
                String filePath = fileStorageService.storeFile(taxIdentificationDocument, 
                    "restaurants/" + restaurantId + "/documents/tax-identification");
                String fullUrl = "/api/files/" + filePath.replace("\\", "/");
                restaurantService.updateTaxIdentificationDocument(restaurantId, fullUrl);
                documentUrls.put("taxIdentificationDocumentUrl", fullUrl);
            }
            
            // Upload Business Operating License
            if (businessOperatingLicense != null && !businessOperatingLicense.isEmpty()) {
                String filePath = fileStorageService.storeFile(businessOperatingLicense, 
                    "restaurants/" + restaurantId + "/documents/operating-license");
                String fullUrl = "/api/files/" + filePath.replace("\\", "/");
                restaurantService.updateBusinessOperatingLicense(restaurantId, fullUrl);
                documentUrls.put("businessOperatingLicenseUrl", fullUrl);
            }
            
            return ResponseEntity.ok(documentUrls);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload documents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
}
