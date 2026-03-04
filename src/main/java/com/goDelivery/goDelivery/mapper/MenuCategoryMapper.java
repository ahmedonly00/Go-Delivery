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

        return MenuCategoryDTO.builder()
                .categoryId(menuCategory.getCategoryId())
                .categoryName(menuCategory.getCategoryName())
                .isActive(menuCategory.getIsActive() != null && menuCategory.getIsActive())
                .createdAt(menuCategory.getCreatedAt())
                .branchId(menuCategory.getBranch() != null ? menuCategory.getBranch().getBranchId() : null)
                .restaurantId(menuCategory.getRestaurant() != null ? menuCategory.getRestaurant().getRestaurantId() : null)
                .build();
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
                .isActive(menuCategory.getIsActive())
                .branchId(menuCategory.getBranch() != null ? menuCategory.getBranch().getBranchId() : null)
                .createdAt(menuCategory.getCreatedAt())
                .build();
    }

    public List<MenuCategoryResponseDTO> toMenuCategoryResponseDTOList(List<MenuCategory> menuCategories) {
        if (menuCategories == null) {
            return null;
        }
        return menuCategories.stream()
                .map(this::toMenuCategoryResponseDTO)
                .collect(Collectors.toList());
    }
}
