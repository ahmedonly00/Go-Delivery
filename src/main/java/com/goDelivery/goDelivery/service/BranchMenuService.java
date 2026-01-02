package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.restaurant.MenuItemDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.model.BranchUsers;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.repository.BranchUsersRepository;
import com.goDelivery.goDelivery.repository.BranchesRepository;
import com.goDelivery.goDelivery.repository.MenuCategoryRepository;
import com.goDelivery.goDelivery.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchMenuService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final BranchesRepository branchesRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final UsersService usersService;
    
    private final String UPLOAD_DIR = "uploads/branch-menu/";

    @Transactional
    public MenuCategoryDTO createMenuCategory(Long branchId, MenuCategoryDTO categoryDTO) {
        // Verify branch access
        verifyBranchAccess(branchId);
        
        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        // Check if category name already exists for this branch
        if (menuCategoryRepository.existsByBranch_BranchIdAndCategoryName(branchId, categoryDTO.getCategoryName())) {
            throw new IllegalArgumentException("Category with this name already exists for this branch");
        }
        
        // Create menu category
        MenuCategory category = new MenuCategory();
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setBranch(branch);
        
        MenuCategory savedCategory = menuCategoryRepository.save(category);
        log.info("Created menu category '{}' for branch {}", categoryDTO.getCategoryName(), branchId);
        
        return convertToDTO(savedCategory);
    }

    @Transactional
    public MenuItemDTO createMenuItem(Long branchId, Long categoryId, MenuItemDTO menuItemDTO,
                                    MultipartFile imageFile) {
        // Verify branch access
        verifyBranchAccess(branchId);
        
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));
        
        // Verify category belongs to the branch
        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Category does not belong to this branch");
        }
        
        // Upload image if provided
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }
        
        // Create menu item
        MenuItem menuItem = new MenuItem();
        menuItem.setMenuItemName(menuItemDTO.getItemName());
        menuItem.setDescription(menuItemDTO.getDescription());
        menuItem.setPrice(menuItemDTO.getPrice());
        menuItem.setCategory(category);
        menuItem.setImage(imageUrl);
        menuItem.setAvailable(menuItemDTO.isAvailable());
        menuItem.setPreparationTime(menuItemDTO.getPreparationTime());
        menuItem.setBranch(category.getBranch());
        
        MenuItem savedItem = menuItemRepository.save(menuItem);
        log.info("Created menu item '{}' for category {} in branch {}", 
                menuItemDTO.getItemName(), categoryId, branchId);
        
        return convertToDTO(savedItem);
    }

    @Transactional(readOnly = true)
    public List<MenuCategoryDTO> getBranchMenuCategories(Long branchId) {
        // Verify branch exists (no access check for public view)
        branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        return menuCategoryRepository.findByBranch_BranchId(branchId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemDTO> getBranchMenuItems(Long branchId, Long categoryId) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));
        
        // Verify category belongs to branch
        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Category does not belong to this branch");
        }
        
        return menuItemRepository.findByCategory_CategoryId(categoryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuCategoryDTO updateMenuCategory(Long branchId, Long categoryId, MenuCategoryDTO categoryDTO) {
        verifyBranchAccess(branchId);
        
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));
        
        // Verify category belongs to branch
        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Category does not belong to this branch");
        }
        
        // Update category
        category.setCategoryName(categoryDTO.getCategoryName());
        
        MenuCategory updatedCategory = menuCategoryRepository.save(category);
        log.info("Updated menu category {} for branch {}", categoryId, branchId);
        
        return convertToDTO(updatedCategory);
    }

    @Transactional
    public void deleteMenuCategory(Long branchId, Long categoryId) {
        verifyBranchAccess(branchId);
        
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));
        
        // Verify category belongs to branch
        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Category does not belong to this branch");
        }
        
        // Check if category has menu items
        if (!category.getMenuItems().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with existing menu items");
        }
        
        menuCategoryRepository.delete(category);
        log.info("Deleted menu category {} from branch {}", categoryId, branchId);
    }

    private void verifyBranchAccess(Long branchId) {
        Object currentUserObj = usersService.getCurrentUser();
        
        // Check if user is a branch user
        if (currentUserObj instanceof BranchUsers) {
            BranchUsers currentUser = (BranchUsers) currentUserObj;
            
            // Verify the branch user belongs to this branch
            if (!currentUser.getBranch().getBranchId().equals(branchId)) {
                throw new UnauthorizedException("You don't have permission to manage this branch's menu");
            }
        }
        // If not a branch user, check if restaurant admin
        // Restaurant admins can also manage branch menus
        else {
            // Allow restaurant admins to proceed
            return;
        }
    }

    private String uploadImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR + fileName);
            
            // Create directory if it doesn't exist
            Files.createDirectories(uploadPath.getParent());
            
            // Save file
            Files.copy(file.getInputStream(), uploadPath);
            
            return "/" + uploadPath.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    private MenuCategoryDTO convertToDTO(MenuCategory category) {
        MenuCategoryDTO dto = new MenuCategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setBranchId(category.getBranch().getBranchId());
        return dto;
    }

    private MenuItemDTO convertToDTO(MenuItem item) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setItemId(item.getMenuItemId());
        dto.setItemName(item.getMenuItemName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setImageUrl(item.getImage());
        dto.setAvailable(item.isAvailable());
        dto.setPreparationTime(item.getPreparationTime());
        dto.setCategoryId(item.getCategory().getCategoryId());
        dto.setBranchId(item.getBranch().getBranchId());
        return dto;
    }
}
