package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.Enum.DistanceUnit;
import com.goDelivery.goDelivery.dtos.delivery.DeliveryFeeCalculationResponse;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryFeeCalculationService {

    private final RestaurantRepository restaurantRepository;
    private final DistanceCalculationService distanceService;

    /**
     * Calculate delivery fee based on customer location
     */
    @Cacheable(value = "deliveryFees", key = "#restaurantId + '-' + #customerLat + '-' + #customerLon")
    public DeliveryFeeCalculationResponse calculateDeliveryFee(
            Long restaurantId,
            double customerLat,
            double customerLon) {

        log.debug("Calculating delivery fee for restaurant {} to location ({}, {})",
                restaurantId, customerLat, customerLon);

        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id: " + restaurantId));

        // Validate restaurant has coordinates
        if (restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            throw new IllegalStateException("Restaurant does not have location coordinates set");
        }

        // Calculate distance
        double distanceKm = distanceService.calculateDistance(
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                customerLat,
                customerLon);

        log.debug("Distance calculated: {} km", distanceKm);

        // Check if within delivery radius
        boolean withinRadius = isWithinDeliveryRadius(restaurant, distanceKm);

        // Calculate fee
        Float deliveryFee = null;
        String message = null;

        if (!withinRadius) {
            message = "Outside delivery area";
            log.info("Customer location is outside delivery radius for restaurant {}", restaurantId);
        } else {
            if (restaurant.getDeliveryType() == DeliveryType.SELF_DELIVERY) {
                deliveryFee = calculateSelfDeliveryFee(restaurant, distanceKm);
                log.debug("Self-delivery fee calculated: {}", deliveryFee);
            } else {
                // SYSTEM_DELIVERY - use system pricing (placeholder for now)
                deliveryFee = 0.0f; // Or fetch from system configuration
                message = "System delivery fee will be calculated at checkout";
                log.debug("System delivery - fee will be calculated at checkout");
            }
        }

        return DeliveryFeeCalculationResponse.builder()
                .deliveryFee(deliveryFee)
                .distanceKm(distanceKm)
                .distanceDisplay(formatDistance(distanceKm))
                .withinRadius(withinRadius)
                .message(message)
                .build();
    }

    /**
     * Calculate fee for SELF_DELIVERY restaurants
     * Formula: baseFee + (distance * perKmFee)
     */
    private Float calculateSelfDeliveryFee(Restaurant restaurant, double distanceKm) {
        Float baseFee = restaurant.getBaseDeliveryFee();
        Float perKmFee = restaurant.getPerKmFee();

        if (baseFee == null || perKmFee == null) {
            // Fallback to flat delivery fee
            log.warn("Restaurant {} missing base/perKm fees, using flat fee",
                    restaurant.getRestaurantId());
            return restaurant.getDeliveryFee();
        }

        // baseFee + (distance * perKmFee)
        float calculatedFee = baseFee + (float) (distanceKm * perKmFee);
        log.debug("Fee calculation: {} + ({} * {}) = {}",
                baseFee, distanceKm, perKmFee, calculatedFee);

        return calculatedFee;
    }

    /**
     * Check if customer is within restaurant's delivery radius
     */
    public boolean isWithinDeliveryRadius(Restaurant restaurant, double distanceKm) {
        if (restaurant.getDeliveryType() == DeliveryType.SYSTEM_DELIVERY) {
            return true; // System delivery has no radius limit
        }

        if (restaurant.getDeliveryRadius() == null) {
            log.warn("Restaurant {} has SELF_DELIVERY but no delivery radius set",
                    restaurant.getRestaurantId());
            return false;
        }

        double restaurantRadiusKm = restaurant.getDeliveryRadius();

        // Convert to km if needed
        if (restaurant.getRadiusUnit() == DistanceUnit.METERS) {
            restaurantRadiusKm = restaurantRadiusKm / 1000.0;
        }

        return distanceKm <= restaurantRadiusKm;
    }

    /**
     * Format distance for display
     */
    private String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            return String.format("%.0f m", distanceKm * 1000);
        }
        return String.format("%.1f km", distanceKm);
    }
}
