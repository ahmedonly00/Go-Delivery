package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.RestaurantDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantSearchRequest;
import com.goDelivery.goDelivery.dtos.restaurant.UpdateOperatingHoursRequest;
import com.goDelivery.goDelivery.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping(value = "/registerRestaurant")
    public ResponseEntity<RestaurantDTO> registerRestaurant(
            @Valid @RequestBody RestaurantDTO restaurantDTO) {
        RestaurantDTO createdRestaurant = restaurantService.registerRestaurant(restaurantDTO);
        return new ResponseEntity<>(createdRestaurant, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{restaurantId}/operating-hours")
    public ResponseEntity<RestaurantDTO> updateOperatingHours(
            @PathVariable Long restaurantId,
            @Valid @RequestBody UpdateOperatingHoursRequest request) {
        RestaurantDTO updatedRestaurant = restaurantService.updateOperatingHours(restaurantId, request);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @GetMapping(value = "/getRestaurantById/{restaurantId}")
    public ResponseEntity<RestaurantDTO> getRestaurantById(@PathVariable Long restaurantId) {
        RestaurantDTO restaurant = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant);
    }

    @PutMapping(value = "/updateRestaurant/{restaurantId}")
    public ResponseEntity<RestaurantDTO> updateRestaurant(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantDTO restaurantDTO) {
        RestaurantDTO updatedRestaurant = restaurantService.updateRestaurant(restaurantId, restaurantDTO);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @GetMapping(value = "/getRestaurantsByLocation/{location}")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByLocation(@PathVariable String location) {
        List<RestaurantDTO> restaurants = restaurantService.getRestaurantsByLocation(location);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping(value = "/getRestaurantsByCuisineType/{cuisineType}")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByCuisineType(@PathVariable String cuisineType) {
        List<RestaurantDTO> restaurants = restaurantService.getRestaurantsByCuisineType(cuisineType);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping(value = "/searchRestaurants")
    public ResponseEntity<List<RestaurantDTO>> searchRestaurants(@Valid @RequestBody RestaurantSearchRequest searchRequest) {
        List<RestaurantDTO> restaurants = restaurantService.searchRestaurants(searchRequest);
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping(value = "/getAllActiveRestaurants")
    public ResponseEntity<List<RestaurantDTO>> getAllActiveRestaurants() {
        List<RestaurantDTO> restaurants = restaurantService.getAllActiveRestaurants();
        return ResponseEntity.ok(restaurants);
    }
    
}
