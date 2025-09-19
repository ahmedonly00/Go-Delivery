package com.goDelivery.goDelivery.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.mapper.MenuCategoryMapper;
import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.repository.MenuCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;

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

    public MenuCategoryDTO getMenuCategoryByName(String categoryName){
        MenuCategory menuCategory = menuCategoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new RuntimeException("MenuCategory not found"));
        return menuCategoryMapper.toMenuCategoryDTO(menuCategory);
    }

    
}
