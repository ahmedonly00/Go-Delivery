package com.goDelivery.goDelivery.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goDelivery.goDelivery.dtos.menu.MenuItemDTO;
import com.goDelivery.goDelivery.mapper.MenuItemMapper;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.repository.MenuItemRepository;

@Service
public class MenuItemService {
    
    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuItemMapper menuItemMapper;
    
    public MenuItemDTO createMenuItem(MenuItemDTO menuItemDTO){
        MenuItem menuItem = menuItemMapper.toMenuItem(menuItemDTO);
        return menuItemMapper.toMenuItemDTO(menuItemRepository.save(menuItem));
    }
    
    public MenuItemDTO updateMenuItem(Long itemId, MenuItemDTO menuItemDTO){
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("MenuItem not found"));
        menuItem.setItemName(menuItemDTO.getItemName());
        menuItem.setDescription(menuItemDTO.getDescription());
        menuItem.setPrice(menuItemDTO.getPrice());
        menuItem.setImage(menuItemDTO.getImage());
        menuItem.setIngredients(menuItemDTO.getIngredients());
        menuItem.setPreparationTime(menuItemDTO.getPreparationTime());
        menuItem.setPreparationScore(menuItemDTO.getPreparationScore());
        menuItem.setRestaurant(menuItemDTO.getRestaurant());
        menuItem.setVariants(menuItemDTO.getVariants());
        menuItem.setCategory(menuItemDTO.getCategory());
        menuItem.setOrderItems(menuItemDTO.getOrderItems());
        menuItem.setUpdatedAt(LocalDate.now());
        return menuItemMapper.toMenuItemDTO(menuItemRepository.save(menuItem));
    }   
    
    public void deleteMenuItem(Long itemId){
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("MenuItem not found"));
        menuItemRepository.delete(menuItem);
    }
    
    public MenuItemDTO getMenuItemById(Long itemId){
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("MenuItem not found"));
        return menuItemMapper.toMenuItemDTO(menuItem);
    }
    
    public List<MenuItemDTO> getAllMenuItems(){
        return menuItemMapper.toMenuItemDTO(menuItemRepository.findAll());
    }
    
    public MenuItemDTO getMenuItemByName(String itemName){
        MenuItem menuItem = menuItemRepository.findByItemName(itemName)
                .orElseThrow(() -> new RuntimeException("MenuItem not found"));
        return menuItemMapper.toMenuItemDTO(menuItem);
    }
    
            
}
