package com.goDelivery.goDelivery.controller;

import java.util.List;

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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/menu-item")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    
    @PostMapping("/createMenuItem/{restaurantId}")
    public ResponseEntity<MenuItemDTO> createMenuItem(@PathVariable Long restaurantId, @RequestBody MenuItemDTO menuItemDTO){
        return ResponseEntity.ok(menuItemService.createMenuItem(menuItemDTO));
    }
    
    @GetMapping("/getAllMenuItems/{restaurantId}")
    public ResponseEntity<List<MenuItemDTO>> getAllMenuItems(@PathVariable Long restaurantId){
        return ResponseEntity.ok(menuItemService.getAllMenuItems());
    }
    
    @GetMapping("/getMenuItemById/{itemId}")
    public ResponseEntity<MenuItemDTO> getMenuItemById(@PathVariable Long itemId){
        return ResponseEntity.ok(menuItemService.getMenuItemById(itemId));
    }
    
    @PutMapping("/updateMenuItem/{itemId}")
    public ResponseEntity<MenuItemDTO> updateMenuItem(@PathVariable Long itemId, @RequestBody MenuItemDTO menuItemDTO){
        return ResponseEntity.ok(menuItemService.updateMenuItem(itemId, menuItemDTO));
    }
    
    @DeleteMapping("/deleteMenuItem/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long itemId){
        menuItemService.deleteMenuItem(itemId);
        return ResponseEntity.noContent().build();
    }
    
    
}
