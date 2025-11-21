package com.goDelivery.goDelivery.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryResponseDTO;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.service.MenuCategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/menu-category")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    
    @PostMapping("/{restaurantId}/create-menu-category")
    public ResponseEntity<?> createMenuCategory(
            @PathVariable Long restaurantId, 
            @RequestBody Map<String, String> request) {
        try {
            log.info("Creating menu category for restaurant ID: {}", restaurantId);
            log.debug("Request payload: {}", request);
            
            // Validate input
            String categoryName = request.get("categoryName");
            if (categoryName == null || categoryName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Category name is required",
                        "field", "categoryName"
                    ));
            }
            
            // Create and populate DTO
            MenuCategoryDTO menuCategoryDTO = new MenuCategoryDTO();
            menuCategoryDTO.setCategoryName(categoryName.trim());
            menuCategoryDTO.setCreatedAt(LocalDate.now());
            
            // Set restaurant
            Restaurant restaurant = new Restaurant();
            restaurant.setRestaurantId(restaurantId);
            menuCategoryDTO.setRestaurant(restaurant);
            
            // Create the category
            MenuCategoryResponseDTO createdCategory = menuCategoryService.createMenuCategory(menuCategoryDTO);
            
            return ResponseEntity.ok(createdCategory);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            log.error("Error creating menu category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Failed to create menu category: " + e.getMessage()
                ));
        }
    }

    @PutMapping("/{categoryId}/update-menu-category")
    public ResponseEntity<MenuCategoryDTO> updateMenuCategory(@PathVariable Long categoryId, @RequestBody MenuCategoryDTO menuCategoryDTO){
        return ResponseEntity.ok(menuCategoryService.updateMenuCategory(categoryId, menuCategoryDTO));
    }

    @DeleteMapping("/{categoryId}/delete-menu-category")
    public ResponseEntity<Void> deleteMenuCategory(@PathVariable Long categoryId){
        menuCategoryService.deleteMenuCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}/get-menu-category")
    public ResponseEntity<MenuCategoryDTO> getMenuCategoryById(@PathVariable Long categoryId){
        return ResponseEntity.ok(menuCategoryService.getMenuCategoryById(categoryId));
    }

    @GetMapping("/all/get-menu-categories")
    public ResponseEntity<List<MenuCategoryDTO>> getAllMenuCategories(){
        return ResponseEntity.ok(menuCategoryService.getAllMenuCategories());
    }

    @GetMapping("/restaurant/{restaurantId}/get-menu-categories-by-restaurant")
    public ResponseEntity<List<MenuCategoryDTO>> getMenuCategoriesByRestaurant(@PathVariable Long restaurantId){
        return ResponseEntity.ok(menuCategoryService.getMenuCategoriesByRestaurant(restaurantId));
    }

    @GetMapping("/name/{categoryName}/get-menu-category-by-name")
    public ResponseEntity<MenuCategoryDTO> getMenuCategoryByName(@PathVariable String categoryName){
        return ResponseEntity.ok(menuCategoryService.getMenuCategoryByName(categoryName));
    }

}
