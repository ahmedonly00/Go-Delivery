package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.delivery.DeliveryFeeCalculationRequest;
import com.goDelivery.goDelivery.dtos.delivery.DeliveryFeeCalculationResponse;
import com.goDelivery.goDelivery.service.DeliveryFeeCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Delivery Fee", description = "Endpoints for calculating delivery fees")
public class DeliveryFeeController {

    private final DeliveryFeeCalculationService feeCalculationService;

    @PostMapping("/calculate-fee")
    @Operation(summary = "Calculate delivery fee", description = "Calculate delivery fee based on customer location and restaurant. "
            +
            "For SELF_DELIVERY restaurants, uses baseFee + (distance * perKmFee). " +
            "Returns fee, distance, and whether customer is within delivery radius.")
    public ResponseEntity<DeliveryFeeCalculationResponse> calculateDeliveryFee(
            @Valid @RequestBody DeliveryFeeCalculationRequest request) {

        DeliveryFeeCalculationResponse response = feeCalculationService.calculateDeliveryFee(
                request.getRestaurantId(),
                request.getCustomerLatitude(),
                request.getCustomerLongitude());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurants/{restaurantId}/calculate-fee")
    @Operation(summary = "Calculate delivery fee (GET)", description = "Calculate delivery fee using query parameters. "
            +
            "Useful for quick checks without POST request.")
    public ResponseEntity<DeliveryFeeCalculationResponse> calculateDeliveryFeeGet(
            @PathVariable Long restaurantId,
            @RequestParam double latitude,
            @RequestParam double longitude) {

        DeliveryFeeCalculationResponse response = feeCalculationService.calculateDeliveryFee(
                restaurantId,
                latitude,
                longitude);

        return ResponseEntity.ok(response);
    }
}
