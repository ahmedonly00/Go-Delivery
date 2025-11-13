package com.goDelivery.goDelivery.config;

import com.goDelivery.goDelivery.model.MenuCategoryTemplate;
import com.goDelivery.goDelivery.repository.MenuCategoryTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class MenuCategoryTemplateInitializer implements CommandLineRunner {

    private final MenuCategoryTemplateRepository templateRepository;

    @Override
    public void run(String... args) throws Exception {
        if (templateRepository.count() == 0) {
            log.info("Initializing menu category templates...");
            initializeTemplates();
            log.info("Menu category templates initialization completed");
        } else {
            log.info("Menu category templates already exist, skipping initialization");
        }
    }

    private void initializeTemplates() {
        // Appetizers & Starters
        createTemplate("Appetizers", "Small dishes served before the main course", 1);

        // Main Courses
        createTemplate("Main Courses", "Primary dishes and entrees", 4);

        // Specific Cuisines
        createTemplate("Pizza", "Various pizza options", 9);
        createTemplate("Burgers", "Burgers and sandwiches", 11);
        createTemplate("Seafood", "Fresh fish and seafood", 12);
        createTemplate("Chicken", "Chicken-based dishes", 13);
        createTemplate("Vegetarian", "Meat-free options", 14);
        
        // Soups & Stews
        createTemplate("Soups", "Hot and cold soups", 21);

        // Desserts
        createTemplate("Desserts", "Sweet treats and desserts", 27);

        // Beverages
        createTemplate("Beverages", "Drinks and refreshments", 30);
        createTemplate("Smoothies", "Fruit smoothies and shakes", 33);
        createTemplate("Fresh Juices", "Freshly squeezed juices", 34);
        
        // Specialty
        createTemplate("Daily Special", "Today's special menu", 36);

        // African Cuisine
        createTemplate("African Dishes", "Traditional African cuisine", 39);
        createTemplate("Mozambican Cuisine", "Traditional Mozambican dishes", 41);

        log.info("Created {} menu category templates", templateRepository.count());
    }

    private void createTemplate(String name, String description, int sortOrder) {
        MenuCategoryTemplate template = MenuCategoryTemplate.builder()
                .categoryName(name)
                .description(description)
                .sortOrder(sortOrder)
                .isActive(true)
                .build();
        templateRepository.save(template);
    }
}
