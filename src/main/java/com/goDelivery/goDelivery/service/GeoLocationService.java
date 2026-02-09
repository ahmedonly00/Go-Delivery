package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.Enum.DistanceUnit;
import com.goDelivery.goDelivery.model.Bikers;
import com.goDelivery.goDelivery.model.Coordinates;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.BikersRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoLocationService {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    private final GeocodingService geocodingService;
    private final DistanceCalculationService distanceService;
    private final BikersRepository bikersRepository;
    private final RestaurantRepository restaurantRepository;

    // Find nearest bikers
    public List<Bikers> findNearestBikers(String pickupAddress, double maxDistanceKm) {
        try {
            // Get coordinates for the pickup address
            Coordinates pickupCoords = geocodingService.geocodeAddress(pickupAddress);

            // Get all available bikers
            List<Bikers> availableBikers = bikersRepository.findAvailableBikers();

            // Calculate distances and filter by max distance
            return availableBikers.parallelStream()
                    .filter(biker -> biker.getCurrentLatitude() != null && biker.getCurrentLongitude() != null)
                    .map(biker -> {
                        double distance = distanceService.calculateDistance(
                                pickupCoords.getLatitude(),
                                pickupCoords.getLongitude(),
                                biker.getCurrentLatitude(),
                                biker.getCurrentLongitude());
                        biker.setDistanceFromPickup(distance);
                        return biker;
                    })
                    .filter(biker -> biker.getDistanceFromPickup() <= maxDistanceKm)
                    .sorted(Comparator.comparingDouble(Bikers::getDistanceFromPickup))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error finding nearest bikers for address: " + pickupAddress, e);
            throw new RuntimeException("Error finding available bikers. Please try again.");
        }
    }

    public int calculateEta(double originLat, double originLng, double destLat, double destLng) {
        // In a real implementation, this would use a routing service
        // For now, we'll use a simple calculation based on distance and average speed
        double distance = distanceService.calculateDistance(originLat, originLng, destLat, destLng);
        double averageSpeedKmh = 30.0; // Average speed in km/h
        return (int) Math.ceil((distance / averageSpeedKmh) * 60); // Convert to minutes
    }

    /**
     * Finds restaurants within a specified radius of the given coordinates
     * Enhanced with delivery radius validation and caching
     */
    @Cacheable(value = "nearbyRestaurants", key = "#latitude + '-' + #longitude + '-' + #radiusKm")
    public List<Restaurant> findNearbyRestaurants(double latitude, double longitude, double radiusKm) {
        // Validate coordinates
        validateCoordinates(latitude, longitude, radiusKm);

        log.debug("Finding restaurants near ({}, {}) within {} km", latitude, longitude, radiusKm);

        // First, get all approved restaurants with coordinates
        List<Restaurant> allRestaurants = restaurantRepository
                .findByIsApprovedTrueAndLatitudeIsNotNullAndLongitudeIsNotNull();

        // Calculate distance for each restaurant and filter by radius
        List<Restaurant> nearbyRestaurants = allRestaurants.parallelStream()
                .map(restaurant -> {
                    double distance = distanceService.calculateDistance(
                            latitude,
                            longitude,
                            restaurant.getLatitude(),
                            restaurant.getLongitude());
                    restaurant.setDistanceFromUser(distance);
                    return restaurant;
                })
                .filter(restaurant -> isRestaurantAvailable(restaurant, radiusKm))
                .sorted(Comparator.comparingDouble(Restaurant::getDistanceFromUser))
                .collect(Collectors.toList());

        log.debug("Found {} restaurants within radius", nearbyRestaurants.size());
        return nearbyRestaurants;
    }

    /**
     * Check if restaurant is available for delivery to customer
     * Validates both search radius and restaurant's delivery radius
     */
    private boolean isRestaurantAvailable(Restaurant restaurant, double searchRadiusKm) {
        double distance = restaurant.getDistanceFromUser();

        // Check if within search radius
        if (distance > searchRadiusKm) {
            return false;
        }

        // For SELF_DELIVERY, check restaurant's delivery radius
        if (restaurant.getDeliveryType() == DeliveryType.SELF_DELIVERY) {
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

            return distance <= restaurantRadiusKm;
        }

        // SYSTEM_DELIVERY restaurants can deliver anywhere within search radius
        return true;
    }

    /**
     * Validate coordinates and radius
     */
    private void validateCoordinates(double latitude, double longitude, double radiusKm) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }

        if (radiusKm <= 0 || radiusKm > 50) {
            throw new IllegalArgumentException("Radius must be between 0 and 50 km");
        }
    }

    /**
     * Finds restaurants by name within a specified radius of the given coordinates
     */
    public List<Restaurant> findNearbyRestaurantsByName(String restaurantName, double latitude, double longitude,
            double radiusKm) {
        // Validate coordinates
        validateCoordinates(latitude, longitude, radiusKm);

        // First, get all approved restaurants with coordinates and matching name
        List<Restaurant> matchingRestaurants = restaurantRepository
                .findByRestaurantNameContainingIgnoreCaseAndIsApprovedTrueAndLatitudeIsNotNullAndLongitudeIsNotNull(
                        restaurantName);

        // Calculate distance for each restaurant and filter by radius
        return matchingRestaurants.parallelStream()
                .map(restaurant -> {
                    double distance = distanceService.calculateDistance(
                            latitude,
                            longitude,
                            restaurant.getLatitude(),
                            restaurant.getLongitude());
                    restaurant.setDistanceFromUser(distance);
                    return restaurant;
                })
                .filter(restaurant -> isRestaurantAvailable(restaurant, radiusKm))
                .sorted(Comparator.comparingDouble(Restaurant::getDistanceFromUser))
                .collect(Collectors.toList());
    }
}
