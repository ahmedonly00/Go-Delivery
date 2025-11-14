package com.goDelivery.goDelivery.config;

import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.MenuCategoryRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MenuCategoryInitializer {
    
    static {
        log.info("MenuCategoryInitializer class loaded");
    }

    private final MenuCategoryRepository menuCategoryRepository;
    private final RestaurantRepository restaurantRepository;

    @PostConstruct
    public void init() {
        log.info("MenuCategoryInitializer.init() called");
        if (menuCategoryRepository.count() == 0) {
            log.info("Initializing default menu categories...");
            
            // Get all active restaurants to create default categories for each
            List<Restaurant> restaurants = restaurantRepository.findAll();
            
            if (restaurants.isEmpty()) {
                log.warn("No restaurants found. Categories will be created when a restaurant is added.");
                return;
            }

            int totalCategoriesCreated = 0;
            for (Restaurant restaurant : restaurants) {
                if (restaurant.getIsActive()) {
                    int created = createDefaultCategories(restaurant);
                    totalCategoriesCreated += created;
                }
            }
            
            if (totalCategoriesCreated > 0) {
                log.info("Created {} default menu categories for {} restaurants", 
                        totalCategoriesCreated, 
                        restaurants.size());
            } else {
                log.info("No active restaurants found to create categories for.");
            }
        } else {
            log.info("Menu categories already initialized");
        }
    }

    private int createDefaultCategories(Restaurant restaurant) {
        int createdCount = 0;
        
        try {
            // Main Categories
            createdCount += createCategory(restaurant, "Main Course", "Delicious main dishes", 1, "main_course.jpg") ? 1 : 0;
            createdCount += createCategory(restaurant, "Appetizers", "Tasty starters", 2, "appetizers.jpg") ? 1 : 0;
            createdCount += createCategory(restaurant, "Desserts", "Sweet treats", 3, "desserts.jpg") ? 1 : 0;
            createdCount += createCategory(restaurant, "Beverages", "Refreshing drinks", 4, "beverages.jpg") ? 1 : 0;
            createdCount += createCategory(restaurant, "Sides", "Perfect accompaniments", 5, "sides.jpg") ? 1 : 0;
            
            // Cuisine Specific Categories
            createdCount += createCategory(restaurant, "Mozambican Specialties", "Traditional Mozambican dishes", 10, "mozambican.jpg") ? 1 : 0;
            createdCount += createCategory(restaurant, "African Cuisine", "Authentic African flavors", 11, "african.jpg") ? 1 : 0;
            createdCount += createCategory(restaurant, "Seafood", "Fresh from the ocean", 12, "seafood.jpg") ? 1 : 0;
            createdCount += createCategory(restaurant, "Vegetarian", "Meat-free options", 13, "vegetarian.jpg") ? 1 : 0;
            createdCount += createCategory(restaurant, "Daily Specials", "Today's featured items", 14, "specials.jpg") ? 1 : 0;
            
            log.debug("Created {} categories for restaurant: {}", createdCount, restaurant.getRestaurantName());
        } catch (Exception e) {
            log.error("Error creating default categories for restaurant {}: {}", 
                    restaurant.getRestaurantName(), e.getMessage(), e);
        }
        
        return createdCount;
    }

    private boolean createCategory(Restaurant restaurant, String name, String description, int sortOrder, String image) {
        try {
            if (!menuCategoryRepository.existsByRestaurantAndCategoryName(restaurant, name)) {
                MenuCategory category = MenuCategory.builder()
                        .categoryName(name)
                        .description(description)
                        .image(image)
                        .sortOrder(sortOrder)
                        .isActive(true)
                        .restaurant(restaurant)
                        .createdAt(LocalDate.now())
                        .build();
                menuCategoryRepository.save(category);
                log.debug("Created category '{}' for restaurant '{}'", name, restaurant.getRestaurantName());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error creating category '{}' for restaurant '{}': {}", 
                    name, restaurant.getRestaurantName(), e.getMessage(), e);
            return false;
        }
    }
}
