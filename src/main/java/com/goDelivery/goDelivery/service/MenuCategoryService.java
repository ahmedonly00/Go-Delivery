package com.goDelivery.goDelivery.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menucategory.CreateCategoryFromTemplateRequest;
import com.goDelivery.goDelivery.dtos.menucategory.MenuCategoryTemplateResponse;
import com.goDelivery.goDelivery.mapper.MenuCategoryMapper;
import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.MenuCategoryTemplate;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.MenuCategoryRepository;
import com.goDelivery.goDelivery.repository.MenuCategoryTemplateRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuCategoryTemplateRepository templateRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryMapper menuCategoryMapper;


    public MenuCategoryDTO createMenuCategory(MenuCategoryDTO menuCategoryDTO){
        MenuCategory menuCategory = menuCategoryMapper.toMenuCategory(menuCategoryDTO);
        return menuCategoryMapper.toMenuCategoryDTO(menuCategoryRepository.save(menuCategory));
        
    }

    public MenuCategoryDTO updateMenuCategory(Long categoryId, MenuCategoryDTO menuCategoryDTO){
        MenuCategory menuCategory = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("MenuCategory not found"));
        menuCategory.setCategoryName(menuCategoryDTO.getCategoryName());
        menuCategory.setDescription(menuCategoryDTO.getDescription());
        menuCategory.setImage(menuCategoryDTO.getImage());
        menuCategory.setSortOrder(menuCategoryDTO.getSortOrder());
        menuCategory.setActive(true);
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

    // Get all available category templates
    public List<MenuCategoryTemplateResponse> getAllTemplates() {
        List<MenuCategoryTemplate> templates = templateRepository.findByIsActiveTrue();
        return templates.stream()
                .map(this::toTemplateResponse)
                .toList();
    }

    // Create a menu category from a template
    public MenuCategoryDTO createCategoryFromTemplate(CreateCategoryFromTemplateRequest request) {
        // Find template
        MenuCategoryTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + request.getTemplateId()));

        // Find restaurant
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + request.getRestaurantId()));

        // Create menu category from template
        MenuCategory menuCategory = MenuCategory.builder()
                .categoryName(template.getCategoryName())
                .description(template.getDescription())
                .image(request.getCustomImage() != null ? request.getCustomImage() : template.getDefaultImageUrl())
                .sortOrder(template.getSortOrder())
                .isActive(true)
                .createdAt(LocalDate.now())
                .restaurant(restaurant)
                .build();

        MenuCategory savedCategory = menuCategoryRepository.save(menuCategory);
        return menuCategoryMapper.toMenuCategoryDTO(savedCategory);
    }

    // Helper method to convert template to response DTO
    private MenuCategoryTemplateResponse toTemplateResponse(MenuCategoryTemplate template) {
        return MenuCategoryTemplateResponse.builder()
                .templateId(template.getTemplateId())
                .categoryName(template.getCategoryName())
                .description(template.getDescription())
                .defaultImageUrl(template.getDefaultImageUrl())
                .sortOrder(template.getSortOrder())
                .isActive(template.isActive())
                .build();
    }
}
