package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.model.Coordinates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    @Value("${google.maps.api.key:}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Converts an address to geographic coordinates (latitude and longitude)
     * @param address The address to geocode
     * @return Coordinates object containing latitude and longitude
     */
    @SuppressWarnings("unchecked")
    public Coordinates geocodeAddress(String address) {
        try {
            String url = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("maps.googleapis.com")
                    .path("/maps/api/geocode/json")
                    .queryParam("address", address)
                    .queryParam("key", apiKey)
                    .build()
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "OK".equals(response.get("status"))) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (results == null || results.isEmpty()) {
                    throw new RuntimeException("No results found for address: " + address);
                }
                
                Map<String, Object> result = results.get(0);
                Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");
                Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                
                double lat = ((Number) location.get("lat")).doubleValue();
                double lng = ((Number) location.get("lng")).doubleValue();
                
                log.debug("Geocoded address '{}' to coordinates: {}, {}", address, lat, lng);
                return new Coordinates(lat, lng);
            } else {
                String errorMessage = "Geocoding failed for address: " + address;
                if (response != null) {
                    errorMessage += ". Status: " + response.get("status");
                    if (response.containsKey("error_message")) {
                        errorMessage += ". Error: " + response.get("error_message");
                    }
                }
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            log.error("Error during geocoding for address: " + address, e);
            throw new RuntimeException("Error processing address. Please check the address and try again.");
        }
    }
}
