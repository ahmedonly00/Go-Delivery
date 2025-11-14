package com.goDelivery.goDelivery.config;

import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Order(1) // This will run before MenuCategoryInitializer
public class RestaurantInitializer {

    private final RestaurantRepository restaurantRepository;

    @PostConstruct
    public void init() {
        log.info("Checking if restaurants need to be initialized...");
        
        if (restaurantRepository.count() == 0) {
            log.info("Initializing default restaurants...");
            
            // Create a default restaurant if none exist
            Restaurant restaurant = Restaurant.builder()
                    .restaurantName("Default Restaurant")
                    .description("A sample restaurant")
                    .isActive(true)
                    .build();
                    
            restaurantRepository.save(restaurant);
            log.info("Created default restaurant: {}", restaurant.getRestaurantName());
        } else {
            log.info("Restaurants already initialized");
        }
    }
}
