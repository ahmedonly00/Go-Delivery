package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.restaurant.DeliverySettingsRequest;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantDTO;
import com.goDelivery.goDelivery.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Restaurant Delivery", description = "Restaurant delivery management")
public class RestaurantDeliveryController {

    private final RestaurantService restaurantService;

    @PutMapping("/{restaurantId}/delivery-settings")
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public ResponseEntity<RestaurantDTO> updateDeliverySettings(
            @PathVariable Long restaurantId,
            @Valid @RequestBody DeliverySettingsRequest request) {
        
        RestaurantDTO updatedRestaurant = restaurantService.updateDeliverySettings(restaurantId, request);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @GetMapping("/{restaurantId}/delivery-fee")
    public ResponseEntity<Float> getDeliveryFee(@PathVariable Long restaurantId) {
        RestaurantDTO restaurant = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant.getDeliveryFee());
    }
}
