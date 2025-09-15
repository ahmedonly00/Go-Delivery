package com.goDelivery.goDelivery.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.goDelivery.goDelivery.dtos.menu.MenuItemDTO;
import com.goDelivery.goDelivery.model.MenuItem;

@Component
public class MenuItemMapper {

    //MenuItem
    public List<MenuItemDTO> toMenuItemDTO(List<MenuItem> menuItems) {
        if (menuItems == null) {
            return null;
        }
        return menuItems.stream()
                .map(this::toMenuItemDTO)
                .collect(Collectors.toList());
    }

    public MenuItemDTO toMenuItemDTO( MenuItem menuItem){
        if(menuItem == null){
            return null;
        }

        return MenuItemDTO.builder()
                .itemId(menuItem.getItemId())
                .itemName(menuItem.getItemName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .image(menuItem.getImage())
                .ingredients(menuItem.getIngredients())
                .isAvailable(true)
                .preparationTime(menuItem.getPreparationTime())
                .preparationScore(menuItem.getPreparationScore())
                .restaurant(menuItem.getRestaurant())
                .variants(menuItem.getVariants())
                .category(menuItem.getCategory())
                .orderItems(menuItem.getOrderItems())
                .build();
    }

    public MenuItem toMenuItem(MenuItemDTO menuItemDTO){
        if(menuItemDTO == null){
            return null;
        }

        return MenuItem.builder()
                .itemId(menuItemDTO.getItemId())
                .itemName(menuItemDTO.getItemName())
                .description(menuItemDTO.getDescription())
                .price(menuItemDTO.getPrice())
                .image(menuItemDTO.getImage())
                .ingredients(menuItemDTO.getIngredients())
                .isAvailable(true)
                .preparationTime(menuItemDTO.getPreparationTime())
                .preparationScore(menuItemDTO.getPreparationScore())
                .restaurant(menuItemDTO.getRestaurant())
                .variants(menuItemDTO.getVariants())
                .category(menuItemDTO.getCategory())
                .orderItems(menuItemDTO.getOrderItems())
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
    } 
    
}
