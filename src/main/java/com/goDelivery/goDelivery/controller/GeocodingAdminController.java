package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin controller for geocoding operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/geocoding")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Admin - Geocoding", description = "Admin endpoints for geocoding operations")
public class GeocodingAdminController {

    private final RestaurantService restaurantService;

    @PostMapping("/geocode-all-restaurants")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Geocode all restaurants without coordinates", description = "Finds all restaurants with missing latitude/longitude and geocodes their addresses. "
            +
            "This is useful for migrating existing restaurants to support location-based searches.")
    public ResponseEntity<?> geocodeAllRestaurants() {
        log.info("Starting bulk geocoding of all restaurants without coordinates");

        try {
            Map<String, Object> result = restaurantService.geocodeAllRestaurantsWithoutCoordinates();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during bulk geocoding", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to geocode restaurants: " + e.getMessage()));
        }
    }

    @PostMapping("/geocode-restaurant/{restaurantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Geocode a specific restaurant", description = "Manually trigger geocoding for a specific restaurant by ID")
    public ResponseEntity<?> geocodeRestaurant(@PathVariable Long restaurantId) {
        log.info("Manually geocoding restaurant ID: {}", restaurantId);

        try {
            restaurantService.geocodeRestaurantById(restaurantId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Restaurant geocoded successfully"));
        } catch (Exception e) {
            log.error("Error geocoding restaurant {}", restaurantId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
