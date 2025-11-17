package com.goDelivery.goDelivery.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
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

        return MenuCategory.builder()
                .categoryId(menuCategoryDTO.getCategoryId())
                .categoryName(menuCategoryDTO.getCategoryName())
                .createdAt(menuCategoryDTO.getCreatedAt() != null ? menuCategoryDTO.getCreatedAt() : LocalDate.now())
                .restaurant(menuCategoryDTO.getRestaurant())
                .build();
    }
    
}
