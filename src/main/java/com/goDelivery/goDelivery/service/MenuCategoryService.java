package com.goDelivery.goDelivery.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryResponseDTO;
import com.goDelivery.goDelivery.mapper.MenuCategoryMapper;
import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.MenuCategoryRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryMapper menuCategoryMapper;


    @Transactional
    public MenuCategoryResponseDTO createMenuCategory(MenuCategoryDTO menuCategoryDTO) {
        log.debug("Creating menu category: {}", menuCategoryDTO.getCategoryName());
        
        // Validate input
        if (menuCategoryDTO.getCategoryName() == null || 
            menuCategoryDTO.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        
        if (menuCategoryDTO.getRestaurant() == null || 
            menuCategoryDTO.getRestaurant().getRestaurantId() == null) {
            throw new IllegalArgumentException("Restaurant ID is required");
        }
        
        // Check if restaurant exists
        Long restaurantId = menuCategoryDTO.getRestaurant().getRestaurantId();
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> 
                new IllegalArgumentException("Restaurant not found with ID: " + restaurantId)
            );
        
        // Check if category with same name already exists for this restaurant (case sensitive)
        if (menuCategoryRepository.existsByRestaurantAndCategoryName(
                restaurant, menuCategoryDTO.getCategoryName().trim())) {
            throw new IllegalArgumentException("A category with this name already exists for this restaurant");
        }
        
        // Create and save the menu category
        MenuCategory menuCategory = new MenuCategory();
        menuCategory.setCategoryName(menuCategoryDTO.getCategoryName().trim());
        menuCategory.setRestaurant(restaurant);
        menuCategory.setCreatedAt(
            menuCategoryDTO.getCreatedAt() != null ? 
            menuCategoryDTO.getCreatedAt() : 
            LocalDate.now()
        );
        
        MenuCategory savedCategory = menuCategoryRepository.save(menuCategory);
        log.info("Created menu category with ID: {} for restaurant ID: {}", 
            savedCategory.getCategoryId(), restaurantId);
        
        return menuCategoryMapper.toMenuCategoryResponseDTO(savedCategory);
    }

    public MenuCategoryDTO updateMenuCategory(Long categoryId, MenuCategoryDTO menuCategoryDTO){
        MenuCategory menuCategory = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("MenuCategory not found"));
        menuCategory.setCategoryName(menuCategoryDTO.getCategoryName());
        menuCategory.setCreatedAt(LocalDate.now());
        return menuCategoryMapper.toMenuCategoryDTO(menuCategoryRepository.save(menuCategory));
        
    }

    public void deleteMenuCategory(Long categoryId){
        MenuCategory menuCategory = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("MenuCategory not found"));
        menuCategoryRepository.delete(menuCategory);
    }

    public MenuCategoryDTO getMenuCategoryById(Long categoryId){
        MenuCategory menuCategory = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("MenuCategory not found"));
        return menuCategoryMapper.toMenuCategoryDTO(menuCategory);
    }

    public List<MenuCategoryDTO> getAllMenuCategories(){
        return menuCategoryMapper.toMenuCategoryDTO(menuCategoryRepository.findAll());
    }

    public List<MenuCategoryDTO> getMenuCategoriesByRestaurant(Long restaurantId){
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + restaurantId));
        List<MenuCategory> categories = menuCategoryRepository.findByRestaurant(restaurant);
        return menuCategoryMapper.toMenuCategoryDTO(categories);
    }

    public MenuCategoryDTO getMenuCategoryByName(String categoryName){
        MenuCategory menuCategory = menuCategoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new RuntimeException("MenuCategory not found"));
        return menuCategoryMapper.toMenuCategoryDTO(menuCategory);
    }

}