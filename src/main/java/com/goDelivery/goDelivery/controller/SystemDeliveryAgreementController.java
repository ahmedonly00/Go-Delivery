package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.agreement.AcceptAgreementRequest;
import com.goDelivery.goDelivery.dtos.agreement.SystemDeliveryAgreementDTO;
import com.goDelivery.goDelivery.service.SystemDeliveryAgreementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/system-delivery")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "System Delivery Agreement", description = "Endpoints for managing system delivery agreements")
public class SystemDeliveryAgreementController {

    private final SystemDeliveryAgreementService agreementService;

    @GetMapping("/agreement")
    @Operation(summary = "Get current active agreement", description = "Retrieves the current active system delivery agreement with terms and commission details")
    public ResponseEntity<SystemDeliveryAgreementDTO> getCurrentAgreement() {
        log.info("Fetching current active agreement");
        return ResponseEntity.ok(agreementService.getCurrentAgreement());
    }

    @PostMapping("/restaurants/{restaurantId}/accept-agreement")
    @Operation(summary = "Accept system delivery agreement", description = "Restaurant accepts the system delivery agreement. Records acceptance with timestamp and IP address.")
    public ResponseEntity<?> acceptAgreement(
            @PathVariable Long restaurantId,
            @Valid @RequestBody AcceptAgreementRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();
        log.info("Restaurant {} attempting to accept agreement version {} from IP {}",
                restaurantId, request.getAgreementVersion(), ipAddress);

        try {
            agreementService.acceptAgreement(restaurantId, request, ipAddress);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Agreement accepted successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Invalid agreement acceptance request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error accepting agreement for restaurant {}", restaurantId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", "Failed to accept agreement: " + e.getMessage()));
        }
    }

    @GetMapping("/restaurants/{restaurantId}/agreement-status")
    @Operation(summary = "Check if restaurant has accepted agreement", description = "Returns whether the restaurant has accepted the system delivery agreement")
    public ResponseEntity<?> getAgreementStatus(@PathVariable Long restaurantId) {
        log.info("Checking agreement status for restaurant {}", restaurantId);
        boolean accepted = agreementService.hasAcceptedAgreement(restaurantId);
        return ResponseEntity.ok(Map.of(
                "restaurantId", restaurantId,
                "accepted", accepted));
    }
}
