package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.service.GeoLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants/location")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Restaurant Location", description = "Endpoints for finding restaurants by location")
public class RestaurantLocationController {

    private final GeoLocationService geoLocationService;

    @GetMapping("/nearby")
    @Operation(summary = "Find restaurants near a location", 
              description = "Returns a list of restaurants within the specified radius of the given coordinates")
    public ResponseEntity<List<Restaurant>> findNearbyRestaurants(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {
        
        List<Restaurant> restaurants = geoLocationService
                .findNearbyRestaurants(latitude, longitude, radiusKm);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/search")
    @Operation(summary = "Search for restaurants by name near a location",
              description = "Returns a list of restaurants matching the name within the specified radius of the given coordinates")
    public ResponseEntity<List<Restaurant>> searchNearbyRestaurants(
            @RequestParam String restaurantName,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {
        
        List<Restaurant> restaurants = geoLocationService
                .findNearbyRestaurantsByName(restaurantName, latitude, longitude, radiusKm);
        return ResponseEntity.ok(restaurants);
    }
}
