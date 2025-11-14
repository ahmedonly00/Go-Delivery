package com.goDelivery.goDelivery.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.service.MenuCategoryService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/menu-category")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    
    @PostMapping("/{restaurantId}/create-menu-category")
    public ResponseEntity<MenuCategoryDTO> createMenuCategory(@PathVariable Long restaurantId, @RequestBody MenuCategoryDTO menuCategoryDTO){
        return ResponseEntity.ok(menuCategoryService.createMenuCategory(menuCategoryDTO));
    }

    @PutMapping("/{categoryId}/update-menu-category")
    public ResponseEntity<MenuCategoryDTO> updateMenuCategory(@PathVariable Long categoryId, @RequestBody MenuCategoryDTO menuCategoryDTO){
        return ResponseEntity.ok(menuCategoryService.updateMenuCategory(categoryId, menuCategoryDTO));
    }

    @DeleteMapping("/{categoryId}/delete-menu-category")
    public ResponseEntity<Void> deleteMenuCategory(@PathVariable Long categoryId){
        menuCategoryService.deleteMenuCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{categoryId}/get-menu-category")
    public ResponseEntity<MenuCategoryDTO> getMenuCategoryById(@PathVariable Long categoryId){
        return ResponseEntity.ok(menuCategoryService.getMenuCategoryById(categoryId));
    }

    @GetMapping("/all/get-menu-categories")
    public ResponseEntity<List<MenuCategoryDTO>> getAllMenuCategories(){
        return ResponseEntity.ok(menuCategoryService.getAllMenuCategories());
    }

    @GetMapping("/restaurant/{restaurantId}/get-menu-categories-by-restaurant")
    public ResponseEntity<List<MenuCategoryDTO>> getMenuCategoriesByRestaurant(@PathVariable Long restaurantId){
        return ResponseEntity.ok(menuCategoryService.getMenuCategoriesByRestaurant(restaurantId));
    }

    @GetMapping("/name/{categoryName}/get-menu-category-by-name")
    public ResponseEntity<MenuCategoryDTO> getMenuCategoryByName(@PathVariable String categoryName){
        return ResponseEntity.ok(menuCategoryService.getMenuCategoryByName(categoryName));
    }

}
