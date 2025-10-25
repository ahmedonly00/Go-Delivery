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
        createTemplate("Starters", "Light dishes to begin your meal", 2);
        createTemplate("Salads", "Fresh and healthy salad options", 3);
        
        // Main Courses
        createTemplate("Main Courses", "Primary dishes and entrees", 4);
        createTemplate("Grilled", "Grilled meats and vegetables", 5);
        createTemplate("Fried", "Deep-fried and pan-fried dishes", 6);
        createTemplate("Steamed", "Healthy steamed options", 7);
        createTemplate("Baked", "Oven-baked specialties", 8);
        
        // Specific Cuisines
        createTemplate("Pizza", "Various pizza options", 9);
        createTemplate("Pasta", "Italian pasta dishes", 10);
        createTemplate("Burgers", "Burgers and sandwiches", 11);
        createTemplate("Seafood", "Fresh fish and seafood", 12);
        createTemplate("Chicken", "Chicken-based dishes", 13);
        createTemplate("Beef", "Beef and steak options", 14);
        createTemplate("Pork", "Pork dishes", 15);
        createTemplate("Vegetarian", "Meat-free options", 16);
        createTemplate("Vegan", "Plant-based dishes", 17);
        
        // Rice & Noodles
        createTemplate("Rice Dishes", "Rice-based meals", 18);
        createTemplate("Noodles", "Noodle dishes", 19);
        createTemplate("Fried Rice", "Various fried rice options", 20);
        
        // Soups & Stews
        createTemplate("Soups", "Hot and cold soups", 21);
        createTemplate("Stews", "Hearty stew options", 22);
        
        // Breakfast
        createTemplate("Breakfast", "Morning meal options", 23);
        createTemplate("Brunch", "Late morning specialties", 24);
        
        // Sides
        createTemplate("Side Dishes", "Complementary side orders", 25);
        createTemplate("Fries", "French fries and potato sides", 26);
        
        // Desserts
        createTemplate("Desserts", "Sweet treats and desserts", 27);
        createTemplate("Ice Cream", "Ice cream and frozen desserts", 28);
        createTemplate("Cakes", "Cakes and pastries", 29);
        
        // Beverages
        createTemplate("Beverages", "Drinks and refreshments", 30);
        createTemplate("Hot Drinks", "Coffee, tea, and hot beverages", 31);
        createTemplate("Cold Drinks", "Soft drinks and juices", 32);
        createTemplate("Smoothies", "Fruit smoothies and shakes", 33);
        createTemplate("Fresh Juices", "Freshly squeezed juices", 34);
        
        // Specialty
        createTemplate("Chef's Special", "Chef's recommended dishes", 35);
        createTemplate("Daily Special", "Today's special menu", 36);
        createTemplate("Combo Meals", "Value combo packages", 37);
        createTemplate("Kids Menu", "Child-friendly options", 38);
        
        // African Cuisine
        createTemplate("African Dishes", "Traditional African cuisine", 39);
        createTemplate("Rwandan Cuisine", "Traditional Rwandan dishes", 40);
        createTemplate("Mozambican Cuisine", "Traditional Mozambican dishes", 41);
        
        // Fast Food
        createTemplate("Fast Food", "Quick service items", 42);
        createTemplate("Street Food", "Street food favorites", 43);
        
        // Healthy Options
        createTemplate("Healthy Options", "Low-calorie and nutritious meals", 44);
        createTemplate("Gluten-Free", "Gluten-free dishes", 45);
        
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
