package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.*;
import com.goDelivery.goDelivery.service.RestaurantService;
import com.goDelivery.goDelivery.service.FileStorageService;
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
@CrossOrigin(origins = "http://localhost:3000")
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
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile) {
        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                // Store the logo file
                String filePath = fileStorageService.storeFile(logoFile, "restaurants/temp/logo");
                String fullUrl = "/api/files/" + filePath.replace("\\", "/");
                restaurantDTO.setLogoUrl(fullUrl);
            }
            
            RestaurantDTO createdRestaurant = registrationService.completeRestaurantRegistration(
                userDetails.getUsername(), 
                restaurantDTO
            );
            return new ResponseEntity<>(createdRestaurant, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Failed to register restaurant: " + e.getMessage() + "\"}");
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
    
}
