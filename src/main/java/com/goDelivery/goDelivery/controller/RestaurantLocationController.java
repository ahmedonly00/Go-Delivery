package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.RestaurantDTO;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.service.GeoLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/restaurants/location")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Restaurant Location", description = "Endpoints for finding restaurants by location")
public class RestaurantLocationController {

    private final GeoLocationService geoLocationService;
    private final RestaurantMapper restaurantMapper;

    @GetMapping("/nearby")
    @Operation(summary = "Find restaurants near a location with pagination", description = "Returns a paginated list of restaurants within the specified radius of the given coordinates. "
            +
            "Includes distance information, ETA, and supports filtering by cuisine, rating, and delivery fee. " +
            "Only shows restaurants that will actually deliver to the customer location.")
    public ResponseEntity<?> findNearbyRestaurants(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "distance") String sortBy,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Float maxDeliveryFee) {

        // Validate coordinates
        if (latitude < -90 || latitude > 90) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Latitude must be between -90 and 90"));
        }

        if (longitude < -180 || longitude > 180) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Longitude must be between -180 and 180"));
        }

        if (radiusKm <= 0 || radiusKm > 50) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Radius must be between 0 and 50 km"));
        }

        // Get nearby restaurants (already filtered by delivery radius in service)
        List<Restaurant> restaurants = geoLocationService
                .findNearbyRestaurants(latitude, longitude, radiusKm);

        // Apply additional filters
        Stream<Restaurant> stream = restaurants.stream();

        if (cuisineType != null && !cuisineType.isEmpty()) {
            stream = stream.filter(r -> r.getCuisineType().equalsIgnoreCase(cuisineType));
        }

        if (minRating != null) {
            stream = stream.filter(r -> r.getRating() != null && r.getRating() >= minRating);
        }

        if (maxDeliveryFee != null) {
            stream = stream.filter(r -> r.getDeliveryFee() != null && r.getDeliveryFee() <= maxDeliveryFee);
        }

        List<Restaurant> filtered = stream.collect(Collectors.toList());

        // Apply sorting
        switch (sortBy.toLowerCase()) {
            case "rating":
                filtered.sort(Comparator.comparingDouble(Restaurant::getRating).reversed());
                break;
            case "popularity":
                filtered.sort(Comparator.comparingInt(Restaurant::getTotalReviews).reversed());
                break;
            case "distance":
            default:
                // Already sorted by distance from service
                break;
        }

        // Convert to DTOs
        List<RestaurantDTO> dtos = filtered.stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());

        // Paginate
        int start = page * size;
        int end = Math.min(start + size, dtos.size());

        if (start >= dtos.size()) {
            return ResponseEntity.ok(new PageImpl<>(
                    Collections.emptyList(),
                    PageRequest.of(page, size),
                    dtos.size()));
        }

        List<RestaurantDTO> pageContent = dtos.subList(start, end);

        Page<RestaurantDTO> pageResult = new PageImpl<>(
                pageContent,
                PageRequest.of(page, size),
                dtos.size());

        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/search")
    @Operation(summary = "Search for restaurants by name near a location", description = "Returns a list of restaurants matching the name within the specified radius of the given coordinates. "
            +
            "Includes distance information and respects restaurant delivery radius.")
    public ResponseEntity<List<RestaurantDTO>> searchNearbyRestaurants(
            @RequestParam String restaurantName,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {

        List<Restaurant> restaurants = geoLocationService
                .findNearbyRestaurantsByName(restaurantName, latitude, longitude, radiusKm);

        List<RestaurantDTO> dtos = restaurants.stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
