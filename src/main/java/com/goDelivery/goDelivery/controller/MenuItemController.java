package com.goDelivery.goDelivery.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.menu.MenuItemDTO;
import com.goDelivery.goDelivery.service.MenuItemService;

@RestController
@RequestMapping("/menu-item")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private MenuItemDTO menuItemDTO;

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<MenuItemDTO> createMenuItem(@PathVariable Long restaurantId, @RequestBody MenuItemDTO menuItemDTO){
        return ResponseEntity.ok(menuItemService.createMenuItem(menuItemDTO));
    }
    
    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<List<MenuItemDTO>> getAllMenuItems(@PathVariable Long restaurantId){
        return ResponseEntity.ok(menuItemService.getAllMenuItems());
    }
    
    @GetMapping("/menu-items/{itemId}")
    public ResponseEntity<MenuItemDTO> getMenuItemById(@PathVariable Long itemId){
        return ResponseEntity.ok(menuItemService.getMenuItemById(itemId));
    }
    
    @PutMapping("/menu-items/{itemId}")
    public ResponseEntity<MenuItemDTO> updateMenuItem(@PathVariable Long itemId, @RequestBody MenuItemDTO menuItemDTO){
        return ResponseEntity.ok(menuItemService.updateMenuItem(itemId, menuItemDTO));
    }
    
    @DeleteMapping("/menu-items/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long itemId){
        menuItemService.deleteMenuItem(itemId);
        return ResponseEntity.noContent().build();
    }
    
    
}
