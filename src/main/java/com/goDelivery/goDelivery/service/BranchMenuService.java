package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dto.menu.MenuCategoryWithItemsDTO;
import com.goDelivery.goDelivery.dto.menu.MenuItemPartialUpdateDTO;
import com.goDelivery.goDelivery.dto.menu.MenuProgressiveResponseDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryResponseDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.dtos.menu.UpdateMenuItemRequest;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.exception.ValidationException;
import com.goDelivery.goDelivery.mapper.MenuCategoryMapper;
import com.goDelivery.goDelivery.mapper.MenuItemMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private final MenuItemMapper menuItemMapper;
    private final MenuCategoryMapper menuCategoryMapper;
    private final BranchSecurityService branchSecurityService;
    private final MenuAuditService menuAuditService;
    private final MenuRealtimeService menuRealtimeService;

    private static final String UPLOAD_DIR = "uploads/branch-menu/";

    // ── Inheritance ───────────────────────────────────────────────────────────

    @Transactional
    public List<MenuCategory> inheritRestaurantMenu(Long branchId) {
        log.info("Starting menu inheritance for branch: {}", branchId);

        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        Restaurant restaurant = branch.getRestaurant();

        long existingMenuCount = menuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount > 0) {
            throw new ValidationException("Branch already has menu items. Cannot inherit from restaurant.");
        }

        List<MenuCategory> restaurantCategories = menuCategoryRepository
                .findByRestaurantId(restaurant.getRestaurantId());

        List<MenuCategory> inheritedCategories = new ArrayList<>();

        for (MenuCategory restaurantCategory : restaurantCategories) {
            MenuCategory branchCategory = MenuCategory.builder()
                    .categoryName(restaurantCategory.getCategoryName())
                    .branch(branch)
                    .restaurant(null)
                    .createdAt(LocalDate.now())
                    .build();

            branchCategory = menuCategoryRepository.save(branchCategory);

            List<MenuItem> branchItems = new ArrayList<>();
            for (MenuItem restaurantItem : restaurantCategory.getMenuItems()) {
                MenuItem branchItem = MenuItem.builder()
                        .menuItemName(restaurantItem.getMenuItemName())
                        .description(restaurantItem.getDescription())
                        .price(restaurantItem.getPrice())
                        .image(restaurantItem.getImage())
                        .ingredients(restaurantItem.getIngredients())
                        .isAvailable(restaurantItem.isAvailable())
                        .preparationTime(restaurantItem.getPreparationTime())
                        .preparationScore(restaurantItem.getPreparationScore())
                        .branch(branch)
                        .restaurant(null)
                        .category(branchCategory)
                        .createdAt(LocalDate.now())
                        .updatedAt(LocalDate.now())
                        .build();

                for (MenuItemVariant restaurantVariant : restaurantItem.getVariants()) {
                    MenuItemVariant branchVariant = MenuItemVariant.builder()
                            .variantName(restaurantVariant.getVariantName())
                            .priceModifier(restaurantVariant.getPriceModifier())
                            .menuItem(branchItem)
                            .build();
                    branchItem.getVariants().add(branchVariant);
                }

                branchItems.add(branchItem);
            }

            menuItemRepository.saveAll(branchItems);
            branchCategory.setMenuItems(branchItems);
            inheritedCategories.add(branchCategory);
        }

        log.info("Successfully inherited {} categories and {} items for branch: {}",
                inheritedCategories.size(),
                inheritedCategories.stream().mapToInt(c -> c.getMenuItems().size()).sum(),
                branchId);

        return inheritedCategories;
    }

    // ── Full menu views ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MenuCategory> getBranchMenu(Long branchId) {
        return menuCategoryRepository.findByBranch_BranchId(branchId);
    }

    @Transactional(readOnly = true)
    public MenuProgressiveResponseDTO getMenuProgressive(Long branchId, int page, int size, String categoryName) {
        long existingMenuCount = menuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount == 0) {
            log.info("Branch {} has no menu, inheriting from restaurant first", branchId);
            inheritRestaurantMenu(branchId);
        }

        List<MenuCategory> categories;
        if (categoryName != null && !categoryName.isEmpty()) {
            categories = menuCategoryRepository.findByBranch_BranchIdAndCategoryNameContainingIgnoreCase(
                    branchId, categoryName);
        } else {
            categories = menuCategoryRepository.findByBranch_BranchId(branchId);
        }

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, categories.size());
        List<MenuCategory> paginatedCategories = categories.subList(startIndex, endIndex);

        List<MenuCategoryWithItemsDTO> categoryDTOs = paginatedCategories.stream()
                .map(this::convertToCategoryWithItemsDTO)
                .toList();

        return MenuProgressiveResponseDTO.builder()
                .categories(categoryDTOs)
                .currentPage(page)
                .totalPages((int) Math.ceil((double) categories.size() / size))
                .totalItems(categories.size())
                .hasMore(endIndex < categories.size())
                .nextCursor(endIndex < categories.size() ? String.valueOf(page + 1) : null)
                .prevCursor(page > 0 ? String.valueOf(page - 1) : null)
                .build();
    }

    // ── Categories ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MenuCategoryDTO> getBranchMenuCategories(Long branchId) {
        branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        return menuCategoryRepository.findByBranch_BranchId(branchId).stream()
                .map(category -> {
                    MenuCategoryDTO dto = menuCategoryMapper.toMenuCategoryDTO(category);
                    dto.setBranchId(branchId);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuCategoryResponseDTO createMenuCategory(Long branchId, MenuCategoryDTO categoryDTO) {
        verifyBranchAccess(branchId);

        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        if (menuCategoryRepository.existsByBranch_BranchIdAndCategoryName(branchId, categoryDTO.getCategoryName())) {
            throw new IllegalArgumentException("Category with this name already exists for this branch");
        }

        // Auto-inherit restaurant menu if branch has none yet
        long existingMenuCount = menuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount == 0) {
            log.info("Branch {} has no menu, inheriting from restaurant first", branchId);
            inheritRestaurantMenu(branchId);
        }

        MenuCategory category = MenuCategory.builder()
                .categoryName(categoryDTO.getCategoryName())
                .isActive(categoryDTO.isActive())
                .branch(branch)
                .createdAt(LocalDate.now())
                .build();

        MenuCategory savedCategory = menuCategoryRepository.save(category);
        log.info("Created menu category '{}' for branch {}", categoryDTO.getCategoryName(), branchId);

        return menuCategoryMapper.toMenuCategoryResponseDTO(savedCategory);
    }

    @Transactional
    public MenuCategoryResponseDTO updateMenuCategory(Long branchId, Long categoryId, MenuCategoryDTO categoryDTO) {
        verifyBranchAccess(branchId);

        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));

        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Category does not belong to this branch");
        }

        if (categoryDTO.getCategoryName() != null)
            category.setCategoryName(categoryDTO.getCategoryName());
        MenuCategory updatedCategory = menuCategoryRepository.save(category);
        log.info("Updated menu category {} for branch {}", categoryId, branchId);

        return menuCategoryMapper.toMenuCategoryResponseDTO(updatedCategory);
    }

    @Transactional
    public void deleteMenuCategory(Long branchId, Long categoryId) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Category does not belong to this branch");
        }

        if (!category.getMenuItems().isEmpty()) {
            throw new ValidationException("Cannot delete category with existing items. Remove items first.");
        }

        menuCategoryRepository.delete(category);
        log.info("Deleted menu category {} from branch {}", categoryId, branchId);
    }

    // ── Menu items ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getBranchMenuItems(Long branchId, Long categoryId) {
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));

        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Category does not belong to this branch");
        }

        return menuItemRepository.findByCategory_CategoryId(categoryId).stream()
                .map(menuItemMapper::toMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponse createMenuItem(Long branchId, Long categoryId, MenuItemRequest menuItemRequest,
            MultipartFile imageFile) {
        verifyBranchAccess(branchId);

        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));

        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Category does not belong to this branch");
        }

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        MenuItem menuItem = new MenuItem();
        menuItem.setMenuItemName(menuItemRequest.getMenuItemName());
        menuItem.setDescription(menuItemRequest.getDescription());
        menuItem.setPrice(menuItemRequest.getPrice());
        menuItem.setCategory(category);
        menuItem.setImage(imageUrl);
        menuItem.setAvailable(menuItemRequest.isAvailable());
        menuItem.setPreparationTime(menuItemRequest.getPreparationTime());
        menuItem.setPreparationScore(0);
        menuItem.setBranch(category.getBranch());
        menuItem.setRestaurant(category.getBranch().getRestaurant());
        menuItem.setCreatedAt(LocalDate.now());
        menuItem.setUpdatedAt(LocalDate.now());

        MenuItem savedItem = menuItemRepository.save(menuItem);
        log.info("Created menu item '{}' for category {} in branch {}",
                menuItemRequest.getMenuItemName(), categoryId, branchId);

        return menuItemMapper.toMenuItemResponse(savedItem);
    }

    @Transactional
    public MenuItem updateMenuItem(Long branchId, Long menuItemId,
            UpdateMenuItemRequest updateRequest,
            MultipartFile imageFile,
            HttpServletRequest request) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Menu item does not belong to this branch");
        }

        if (updateRequest.getPrice() != null && !updateRequest.getPrice().equals(menuItem.getPrice())) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, "price",
                    menuItem.getPrice().toString(), updateRequest.getPrice().toString(), "Price update", request);
        }
        if (updateRequest.getIsAvailable() != null && !updateRequest.getIsAvailable().equals(menuItem.isAvailable())) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, "isAvailable",
                    String.valueOf(menuItem.isAvailable()), String.valueOf(updateRequest.getIsAvailable()),
                    "Availability change", request);
        }
        if (updateRequest.getMenuItemName() != null
                && !updateRequest.getMenuItemName().equals(menuItem.getMenuItemName())) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, "menuItemName",
                    menuItem.getMenuItemName(), updateRequest.getMenuItemName(), "Name update", request);
        }
        if (updateRequest.getDescription() != null
                && !updateRequest.getDescription().equals(menuItem.getDescription())) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, "description",
                    menuItem.getDescription(), updateRequest.getDescription(), "Description update", request);
        }

        if (updateRequest.getMenuItemName() != null)
            menuItem.setMenuItemName(updateRequest.getMenuItemName());
        if (updateRequest.getDescription() != null)
            menuItem.setDescription(updateRequest.getDescription());
        if (updateRequest.getPrice() != null)
            menuItem.setPrice(updateRequest.getPrice());
        if (imageFile != null && !imageFile.isEmpty())
            menuItem.setImage(uploadImage(imageFile));
        if (updateRequest.getIngredients() != null)
            menuItem.setIngredients(updateRequest.getIngredients());
        if (updateRequest.getIsAvailable() != null)
            menuItem.setAvailable(updateRequest.getIsAvailable());
        if (updateRequest.getPreparationTime() != null)
            menuItem.setPreparationTime(updateRequest.getPreparationTime());

        menuItem.setUpdatedAt(LocalDate.now());
        MenuItem savedItem = menuItemRepository.save(menuItem);

        menuRealtimeService.broadcastMenuItemUpdate(branchId, savedItem, getCurrentUserEmail());

        return savedItem;
    }

    @Transactional
    public MenuItem partialUpdateMenuItem(Long branchId, Long menuItemId,
            MenuItemPartialUpdateDTO partialUpdate,
            HttpServletRequest request) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Menu item does not belong to this branch");
        }

        String fieldName = partialUpdate.getUpdateField();
        String oldValue = null;
        String newValue = null;

        if (partialUpdate.getPrice() != null) {
            oldValue = menuItem.getPrice().toString();
            newValue = partialUpdate.getPrice().toString();
            menuItem.setPrice(partialUpdate.getPrice());
        }
        if (partialUpdate.getIsAvailable() != null) {
            oldValue = String.valueOf(menuItem.isAvailable());
            newValue = String.valueOf(partialUpdate.getIsAvailable());
            menuItem.setAvailable(partialUpdate.getIsAvailable());
        }
        if (partialUpdate.getDescription() != null) {
            oldValue = menuItem.getDescription();
            newValue = partialUpdate.getDescription();
            menuItem.setDescription(partialUpdate.getDescription());
        }
        if (partialUpdate.getIngredients() != null) {
            oldValue = menuItem.getIngredients();
            newValue = partialUpdate.getIngredients();
            menuItem.setIngredients(partialUpdate.getIngredients());
        }
        if (partialUpdate.getPreparationTime() != null) {
            oldValue = menuItem.getPreparationTime().toString();
            newValue = partialUpdate.getPreparationTime().toString();
            menuItem.setPreparationTime(partialUpdate.getPreparationTime());
        }

        menuItem.setUpdatedAt(LocalDate.now());
        MenuItem savedItem = menuItemRepository.save(menuItem);

        if (fieldName != null && oldValue != null && newValue != null) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, fieldName,
                    oldValue, newValue, "Auto-save", request);
        }

        menuRealtimeService.broadcastMenuItemUpdate(branchId, savedItem, getCurrentUserEmail());

        return savedItem;
    }

    @Transactional
    public void deleteMenuItem(Long branchId, Long menuItemId, HttpServletRequest request) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Menu item does not belong to this branch");
        }

        menuAuditService.logMenuItemDelete(menuItemId, branchId, menuItem.getMenuItemName(), request);
        menuItemRepository.delete(menuItem);
        menuRealtimeService.broadcastMenuItemRemoved(branchId, menuItemId, getCurrentUserEmail());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void verifyBranchAccess(Long branchId) {
        if (!branchSecurityService.canAccessBranch(branchId, null)) {
            throw new UnauthorizedException("You don't have permission to manage this branch's menu");
        }
    }

    private String uploadImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(uploadPath.getParent());
            Files.copy(file.getInputStream(), uploadPath);
            return "/" + uploadPath.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    private MenuCategoryWithItemsDTO convertToCategoryWithItemsDTO(MenuCategory category) {
        List<MenuCategoryWithItemsDTO.MenuItemDTO> itemDTOs = category.getMenuItems().stream()
                .map(item -> MenuCategoryWithItemsDTO.MenuItemDTO.builder()
                        .menuItemId(item.getMenuItemId())
                        .menuItemName(item.getMenuItemName())
                        .description(item.getDescription())
                        .price(item.getPrice())
                        .image(item.getImage())
                        .ingredients(item.getIngredients())
                        .isAvailable(item.isAvailable())
                        .preparationTime(item.getPreparationTime())
                        .isInherited(true)
                        .build())
                .toList();

        return MenuCategoryWithItemsDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .items(itemDTOs)
                .isInherited(true)
                .itemCount(itemDTOs.size())
                .build();
    }

    private String getCurrentUserEmail() {
        try {
            return "current-user";
        } catch (Exception e) {
            return "system";
        }
    }
}
