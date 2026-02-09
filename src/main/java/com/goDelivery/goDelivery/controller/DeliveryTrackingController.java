package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.tracking.BikerLocationUpdateRequest;
import com.goDelivery.goDelivery.dtos.tracking.DeliveryTrackingResponse;
import com.goDelivery.goDelivery.service.DeliveryTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/delivery-tracking")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Delivery Tracking", description = "Real-time delivery tracking endpoints")
public class DeliveryTrackingController {

    private final DeliveryTrackingService trackingService;

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get current delivery tracking", description = "Retrieves current tracking information for an order including biker location, ETA, and status")
    public ResponseEntity<DeliveryTrackingResponse> getOrderTracking(@PathVariable Long orderId) {
        log.info("Fetching tracking for order {}", orderId);
        return ResponseEntity.ok(trackingService.getOrderTracking(orderId));
    }

    @PostMapping("/update-location")
    @Operation(summary = "Update biker location", description = "Updates biker's current location and triggers WebSocket notification to customer. "
            +
            "Automatically calculates distance to destination and ETA.")
    public ResponseEntity<?> updateLocation(@Valid @RequestBody BikerLocationUpdateRequest request) {
        log.info("Updating location for order {} from biker {}", request.getOrderId(), request.getBikerId());

        try {
            trackingService.updateBikerLocation(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Location updated successfully"));
        } catch (Exception e) {
            log.error("Error updating location for order {}", request.getOrderId(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }
}
