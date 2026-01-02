package com.goDelivery.goDelivery.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.CollectionDisbursementRequest;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.CollectionDisbursementResponse;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementSummaryDTO;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.RestaurantDisbursementSummaryDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.service.DisbursementService;
import com.goDelivery.goDelivery.service.MomoService;
import com.goDelivery.goDelivery.service.RestaurantService;
import com.goDelivery.goDelivery.service.UsersService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final MomoService momoService;

    @PostMapping("/collection-disbursement")
    @PreAuthorize("hasAuthority('DISBURSEMENT_COLLECTION')")
    @Operation(
        summary = "Collect money from one person and distribute it to multiple recipients",
        description = "Collect money from the specified MSISDN and distribute it to multiple recipients",
        responses = {
            @ApiResponse(responseCode = "200", description = "Collection initiated successfully",
                content = @Content(schema = @Schema(implementation = CollectionDisbursementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<CollectionDisbursementResponse> initiateCollectionDisbursement(
            @Parameter(description = "Collection and disbursement details") 
            @Valid @RequestBody CollectionDisbursementRequest request) {
        
        try {
            CollectionDisbursementResponse response = momoService.initiateCollectionDisbursement(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(CollectionDisbursementResponse.builder()
                    .status("FAILED")
                    .message("Failed to process collection-disbursement: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/restaurant/summary")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CASHIER')")
    public ResponseEntity<RestaurantDisbursementSummaryDTO> getRestaurantDisbursementSummary() {
        // Get the current user's restaurant
        RestaurantUsers currentUser = userService.getCurrentUser();
        Restaurant restaurant = currentUser.getRestaurant();
        if (restaurant == null) {
            throw new ResourceNotFoundException("Restaurant not found for current user");
        }
        
        return ResponseEntity.ok(disbursementService.getRestaurantDisbursementSummary(restaurant.getRestaurantId()));
    }

    @GetMapping("/restaurant/transactions")
    @PreAuthorize("hasAnyRole('RESTAURANT_ADMIN', 'CASHIER')")
    public ResponseEntity<List<DisbursementSummaryDTO>> getRestaurantDisbursements() {
        RestaurantUsers currentUser = userService.getCurrentUser();
        Restaurant restaurant = currentUser.getRestaurant();
        if (restaurant == null) {
            throw new ResourceNotFoundException("Restaurant not found for current user");
        }
        
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
