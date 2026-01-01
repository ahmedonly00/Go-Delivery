package com.goDelivery.goDelivery.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementSummaryDTO;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.RestaurantDisbursementSummaryDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.service.DisbursementService;
import com.goDelivery.goDelivery.service.RestaurantService;
import com.goDelivery.goDelivery.service.UsersService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/disbursements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Disbursement", description = "Disbursement management")
public class DisbursementController {
    private final DisbursementService disbursementService;
    private final RestaurantService restaurantService;
    private final UsersService userService;

    @GetMapping("/restaurant/summary")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CASHIER')")
    public ResponseEntity<RestaurantDisbursementSummaryDTO> getRestaurantDisbursementSummary() {
        // Get the current user's restaurant
        RestaurantUsers currentUser = userService.getCurrentUser();
        Restaurant restaurant = restaurantService.getRestaurantByUser(currentUser)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for current user"));
        
        return ResponseEntity.ok(disbursementService.getRestaurantDisbursementSummary(restaurant.getRestaurantId()));
    }

    @GetMapping("/restaurant/transactions")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CASHIER')")
    public ResponseEntity<List<DisbursementSummaryDTO>> getRestaurantDisbursements() {
        RestaurantUsers currentUser = userService.getCurrentUser();
        Restaurant restaurant = restaurantService.getRestaurantByUser(currentUser)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for current user"));
        
        return ResponseEntity.ok(disbursementService.getDisbursementsForRestaurant(restaurant.getRestaurantId()));
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<RestaurantDisbursementSummaryDTO>> getAllRestaurantSummaries() {
        return ResponseEntity.ok(disbursementService.getRestaurantDisbursementSummaries());
    }

    @GetMapping("/admin/transactions")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<List<DisbursementSummaryDTO>> getAllDisbursements() {
        return ResponseEntity.ok(disbursementService.getAllDisbursements());
    }
}
