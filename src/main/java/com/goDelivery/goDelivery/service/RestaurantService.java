package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.restaurant.*;
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
        Restaurant existingRestaurant = restaurantRepository.findByRestaurantId(restaurantId)
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
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
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
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        return restaurantMapper.toRestaurantDTO(restaurant);
    }

    public RestaurantDTO updateOperatingHours(Long restaurantId, UpdateOperatingHoursRequest request) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        
        OperatingHours operatingHours = restaurant.getOperatingHours();
        if (operatingHours == null) {
            operatingHours = new OperatingHours();
            operatingHours.setRestaurant(restaurant);
        }
        
        // Initialize all days as closed first
        operatingHours.setMondayOpen(null);
        operatingHours.setMondayClose(null);
        operatingHours.setTuesdayOpen(null);
        operatingHours.setTuesdayClose(null);
        operatingHours.setWednesdayOpen(null);
        operatingHours.setWednesdayClose(null);
        operatingHours.setThursdayOpen(null);
        operatingHours.setThursdayClose(null);
        operatingHours.setFridayOpen(null);
        operatingHours.setFridayClose(null);
        operatingHours.setSaturdayOpen(null);
        operatingHours.setSaturdayClose(null);
        operatingHours.setSundayOpen(null);
        operatingHours.setSundayClose(null);
        
        // Update operating hours from request
        if (request.getTimeSlots() != null) {
            for (UpdateOperatingHoursRequest.TimeSlot timeSlot : request.getTimeSlots()) {
                if (timeSlot.isOpen() && timeSlot.getDayOfWeek() != null) {
                    String openTime = timeSlot.getOpenTime() != null ? timeSlot.getOpenTime().toString() : null;
                    String closeTime = timeSlot.getCloseTime() != null ? timeSlot.getCloseTime().toString() : null;
                    
                    switch (timeSlot.getDayOfWeek()) {
                        case MONDAY:
                            operatingHours.setMondayOpen(openTime);
                            operatingHours.setMondayClose(closeTime);
                            break;
                        case TUESDAY:
                            operatingHours.setTuesdayOpen(openTime);
                            operatingHours.setTuesdayClose(closeTime);
                            break;
                        case WEDNESDAY:
                            operatingHours.setWednesdayOpen(openTime);
                            operatingHours.setWednesdayClose(closeTime);
                            break;
                        case THURSDAY:
                            operatingHours.setThursdayOpen(openTime);
                            operatingHours.setThursdayClose(closeTime);
                            break;
                        case FRIDAY:
                            operatingHours.setFridayOpen(openTime);
                            operatingHours.setFridayClose(closeTime);
                            break;
                        case SATURDAY:
                            operatingHours.setSaturdayOpen(openTime);
                            operatingHours.setSaturdayClose(closeTime);
                            break;
                        case SUNDAY:
                            operatingHours.setSundayOpen(openTime);
                            operatingHours.setSundayClose(closeTime);
                            break;
                    }
                }
            }
        }
        // Save the updated operating hours
        operatingHours = operatingHoursRepository.save(operatingHours);
        restaurant.setOperatingHours(operatingHours);
        restaurant.setUpdatedAt(LocalDate.now());
        restaurant = restaurantRepository.save(restaurant);
        
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
