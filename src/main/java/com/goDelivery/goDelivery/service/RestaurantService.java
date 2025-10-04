package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.restaurant.RestaurantDTO;
import com.goDelivery.goDelivery.dtos.restaurant.RestaurantSearchRequest;
import com.goDelivery.goDelivery.dtos.restaurant.UpdateOperatingHoursRequest;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.OperatingHours;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.OperatingHoursRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final OperatingHoursRepository operatingHoursRepository;
    private final RestaurantMapper restaurantMapper;

    public RestaurantDTO registerRestaurant(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = restaurantMapper.toRestaurant(restaurantDTO);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toRestaurantDTO(savedRestaurant);
    }
    
    public RestaurantDTO updateRestaurant(Long restaurantId, RestaurantDTO restaurantDTO) {
        Restaurant existingRestaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        // Update fields from DTO
        existingRestaurant.setRestaurantName(restaurantDTO.getRestaurantName());
        existingRestaurant.setLocation(restaurantDTO.getLocation());
        existingRestaurant.setCuisineType(restaurantDTO.getCuisineType());
        existingRestaurant.setEmail(restaurantDTO.getEmail());
        existingRestaurant.setPhoneNumber(restaurantDTO.getPhoneNumber());
        existingRestaurant.setLogoUrl(restaurantDTO.getLogoUrl());
        existingRestaurant.setIsActive(restaurantDTO.isActive());
        existingRestaurant.setUpdatedAt(java.time.LocalDate.now());

        Restaurant updatedRestaurant = restaurantRepository.save(existingRestaurant);
        return restaurantMapper.toRestaurantDTO(updatedRestaurant);
    }

    public List<RestaurantDTO> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurantMapper.toRestaurantDTO(restaurants);
    }

    public RestaurantDTO updateRestaurantLogo(Long restaurantId, String logoUrl) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        restaurant.setLogoUrl(logoUrl);
        restaurant.setUpdatedAt(LocalDate.now());
        return restaurantMapper.toRestaurantDTO(restaurantRepository.save(restaurant));
    }

    public List<RestaurantDTO> getRestaurantsByLocation(String location) {
        List<Restaurant> restaurants = restaurantRepository.findByLocation(location);
        return restaurantMapper.toRestaurantDTO(restaurants);
    }

    public List<RestaurantDTO> getRestaurantsByCuisineType(String cuisineType) {
        List<Restaurant> restaurants = restaurantRepository.findByCuisineType(cuisineType);
        return restaurantMapper.toRestaurantDTO(restaurants);
    }

    //Search and filter restaurants based on various criteria
    public List<RestaurantDTO> searchRestaurants(RestaurantSearchRequest searchRequest) {
        // Start with all active restaurants
        List<Restaurant> restaurants = restaurantRepository.findByIsActive(true);
        
        // Apply filters
        if (searchRequest.getLocation() != null && !searchRequest.getLocation().isEmpty()) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getLocation().equalsIgnoreCase(searchRequest.getLocation()))
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getCuisineType() != null && !searchRequest.getCuisineType().isEmpty()) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getCuisineType().equalsIgnoreCase(searchRequest.getCuisineType()))
                    .collect(Collectors.toList());
        }
        
        if (searchRequest.getMinRating() != null) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getRating() != null && r.getRating() >= searchRequest.getMinRating())
                    .collect(Collectors.toList());
        }
                
        // Apply sorting
        if (searchRequest.getSortBy() != null && !searchRequest.getSortBy().isEmpty()) {
            switch (searchRequest.getSortBy().toLowerCase()) {
                case "rating":
                    restaurants.sort((r1, r2) -> Float.compare(
                            r2.getRating() != null ? r2.getRating() : 0,
                            r1.getRating() != null ? r1.getRating() : 0
                    ));
                    break;
                case "popularity":
                    restaurants.sort((r1, r2) -> Integer.compare(
                            r2.getTotalOrders() != null ? r2.getTotalOrders() : 0,
                            r1.getTotalOrders() != null ? r1.getTotalOrders() : 0
                    ));
                    break;
            }
        }

        return restaurants.stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());
    }

   
    public RestaurantDTO getRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        return restaurantMapper.toRestaurantDTO(restaurant);
    }

    public RestaurantDTO updateOperatingHours(Long restaurantId, UpdateOperatingHoursRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        
        OperatingHours operatingHours = restaurant.getOperatingHours();
        if (operatingHours == null) {
            operatingHours = new OperatingHours();
            operatingHours.setRestaurant(restaurant);
        }
        
        // Update operating hours
        operatingHours.setMondayOpen(request.getMondayOpen());
        operatingHours.setMondayClose(request.getMondayClose());
        operatingHours.setTuesdayOpen(request.getTuesdayOpen());
        operatingHours.setTuesdayClose(request.getTuesdayClose());
        operatingHours.setWednesdayOpen(request.getWednesdayOpen());
        operatingHours.setWednesdayClose(request.getWednesdayClose());
        operatingHours.setThursdayOpen(request.getThursdayOpen());
        operatingHours.setThursdayClose(request.getThursdayClose());
        operatingHours.setFridayOpen(request.getFridayOpen());
        operatingHours.setFridayClose(request.getFridayClose());
        operatingHours.setSaturdayOpen(request.getSaturdayOpen());
        operatingHours.setSaturdayClose(request.getSaturdayClose());
        operatingHours.setSundayOpen(request.getSundayOpen());
        operatingHours.setSundayClose(request.getSundayClose());
        
        // Save the updated operating hours
        operatingHoursRepository.save(operatingHours);
        
        // Update the restaurant's updatedAt timestamp
        restaurant.setUpdatedAt(LocalDate.now());
        restaurantRepository.save(restaurant);
        
        return restaurantMapper.toRestaurantDTO(restaurant);
    }
    
    public List<RestaurantDTO> getAllActiveRestaurants() {
        return restaurantRepository.findByIsActive(true).stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());
    }

    
    public List<RestaurantDTO> getRestaurantsByCuisine(String cuisineType) {
        return restaurantRepository.findByCuisineTypeAndIsActive(cuisineType, true).stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());
    }
}
