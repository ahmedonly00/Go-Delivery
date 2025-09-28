package com.goDelivery.goDelivery.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.model.MenuItem;

@Component
public class MenuItemMapper {

    //MenuItem
    public List<MenuItemResponse> toMenuItemResponse(List<MenuItem> menuItems) {
        if (menuItems == null) {
            return null;
        }
        return menuItems.stream()
                .map(this::toMenuItemResponse)
                .collect(Collectors.toList());
    }

    public MenuItemResponse toMenuItemResponse(MenuItem menuItem) {
        if(menuItem == null){
            return null;
        }

        return MenuItemResponse.builder()
                .id(menuItem.getMenuItemId())
                .menuItemName(menuItem.getMenuItemName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .image(menuItem.getImage())
                .ingredients(menuItem.getIngredients())
                .isAvailable(menuItem.isAvailable())
                .preparationTime(menuItem.getPreparationTime())
                .preparationScore(menuItem.getPreparationScore())
                .restaurantId(menuItem.getRestaurant() != null ? menuItem.getRestaurant().getRestaurantId() : null)
                .categoryId(menuItem.getCategory() != null ? menuItem.getCategory().getCategoryId() : null)
                .categoryName(menuItem.getCategory() != null ? menuItem.getCategory().getCategoryName() : null)
                .createdAt(menuItem.getCreatedAt())
                .updatedAt(menuItem.getUpdatedAt())
                .build();
    }

    public MenuItem toMenuItem(MenuItemResponse menuItemResponse) {
        if(menuItemResponse == null) {
            return null;
        }

        MenuItem menuItem = new MenuItem();
        menuItem.setMenuItemId(menuItemResponse.getId());
        menuItem.setMenuItemName(menuItemResponse.getMenuItemName());
        menuItem.setDescription(menuItemResponse.getDescription());
        menuItem.setPrice(menuItemResponse.getPrice());
        menuItem.setImage(menuItemResponse.getImage());
        menuItem.setIngredients(menuItemResponse.getIngredients());
        menuItem.setAvailable(menuItemResponse.isAvailable());
        menuItem.setPreparationTime(menuItemResponse.getPreparationTime());
        menuItem.setPreparationScore(menuItemResponse.getPreparationScore());
                
        return menuItem;
    } 
    
}
