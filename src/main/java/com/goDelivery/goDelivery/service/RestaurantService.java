package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.restaurant.RestaurantDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;

    public RestaurantDTO registerRestaurant(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = restaurantMapper.toRestaurant(restaurantDTO);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toRestaurantDTO(savedRestaurant);
    }
    

    public RestaurantDTO getRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        return restaurantMapper.toRestaurantDTO(restaurant);
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
        existingRestaurant.setBannerUrl(restaurantDTO.getBannerUrl());
        existingRestaurant.setAveragePreparationTime(restaurantDTO.getAveragePreparationTime());
        existingRestaurant.setDeliveryFee(restaurantDTO.getDeliveryFee());
        existingRestaurant.setMinimumOrderAmount(restaurantDTO.getMinimumOrderAmount());
        existingRestaurant.setActive(restaurantDTO.isActive());
        existingRestaurant.setUpdatedAt(java.time.LocalDate.now());

        Restaurant updatedRestaurant = restaurantRepository.save(existingRestaurant);
        return restaurantMapper.toRestaurantDTO(updatedRestaurant);
    }

    public List<RestaurantDTO> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurantMapper.toRestaurantDTO(restaurants);
    }

    public Restaurant updateRestaurantLogo(Long restaurantId, String logoUrl) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        restaurant.setLogoUrl(logoUrl);
        restaurant.setUpdatedAt(java.time.LocalDate.now());
        return restaurantRepository.save(restaurant);
    }

    public Restaurant updateRestaurantBanner(Long restaurantId, String bannerUrl) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        restaurant.setBannerUrl(bannerUrl);
        restaurant.setUpdatedAt(java.time.LocalDate.now());
        return restaurantRepository.save(restaurant);
    }
}
