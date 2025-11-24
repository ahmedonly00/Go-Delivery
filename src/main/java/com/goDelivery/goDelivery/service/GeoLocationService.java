package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.model.Bikers;
import com.goDelivery.goDelivery.model.Coordinates;
import com.goDelivery.goDelivery.repository.BikersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
                        biker.getCurrentLongitude()
                    );
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
}
