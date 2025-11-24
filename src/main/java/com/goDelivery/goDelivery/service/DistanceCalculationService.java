package com.goDelivery.goDelivery.service;

import org.springframework.stereotype.Service;

/**
 * Service for calculating distances between geographic coordinates
 */
@Service
public class DistanceCalculationService {

    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers

    /**
     * Calculates the distance between two points on Earth using the Haversine formula
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine formula
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculates the driving distance between two points using a routing service
     * Note: This is a placeholder. In a real implementation, this would call an external API
     * like Google Maps Distance Matrix API or OSRM (Open Source Routing Machine)
     */
    public double calculateDrivingDistance(double originLat, double originLon, 
                                         double destLat, double destLon) {
        // In a real implementation, this would call an external API
        // For now, we'll return the straight-line distance as an approximation
        return calculateDistance(originLat, originLon, destLat, destLon) * 1.3; // Rough estimate
    }
}
