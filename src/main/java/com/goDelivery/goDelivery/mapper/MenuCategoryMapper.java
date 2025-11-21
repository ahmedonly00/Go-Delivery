package com.goDelivery.goDelivery.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryResponseDTO;
import com.goDelivery.goDelivery.model.MenuCategory;

@Component
public class MenuCategoryMapper {

    public List<MenuCategoryDTO> toMenuCategoryDTO(List<MenuCategory> menuCategories) {
        if (menuCategories == null) {
            return null;
        }
        return menuCategories.stream()
                .map(this::toMenuCategoryDTO)
                .collect(Collectors.toList());
    }

    public MenuCategoryDTO toMenuCategoryDTO(MenuCategory menuCategory){
        if(menuCategory == null){
            return null;
        }

        MenuCategoryDTO.MenuCategoryDTOBuilder builder = MenuCategoryDTO.builder()
                .categoryId(menuCategory.getCategoryId())
                .categoryName(menuCategory.getCategoryName())
                .createdAt(menuCategory.getCreatedAt())
                .restaurant(menuCategory.getRestaurant());
                
        // Set menuItems if not null
        if (menuCategory.getMenuItems() != null) {
            builder.menuItems(menuCategory.getMenuItems());
        }
        
        return builder.build();
    }

    public MenuCategory toMenuCategory(MenuCategoryDTO menuCategoryDTO){
        if(menuCategoryDTO == null){
            return null;
        }

        MenuCategory menuCategory = new MenuCategory();
        menuCategory.setCategoryName(menuCategoryDTO.getCategoryName());
        menuCategory.setCreatedAt(menuCategoryDTO.getCreatedAt());
        
        return menuCategory;
    }
    
    public MenuCategoryResponseDTO toMenuCategoryResponseDTO(MenuCategory menuCategory) {
        if (menuCategory == null) {
            return null;
        }

        return MenuCategoryResponseDTO.builder()
            .categoryId(menuCategory.getCategoryId())
            .categoryName(menuCategory.getCategoryName())
            .createdAt(menuCategory.getCreatedAt())
            .restaurantId(menuCategory.getRestaurant() != null ? 
                menuCategory.getRestaurant().getRestaurantId() : null)
            .build();
    }
}
