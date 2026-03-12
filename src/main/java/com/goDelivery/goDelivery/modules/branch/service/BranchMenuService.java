package com.goDelivery.goDelivery.modules.branch.service;

import com.goDelivery.goDelivery.dto.menu.MenuItemPartialUpdateDTO;
import com.goDelivery.goDelivery.dto.menu.MenuProgressiveResponseDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuCategoryResponseDTO;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.dtos.menu.UpdateMenuItemRequest;
import com.goDelivery.goDelivery.dto.menu.MenuCategoryWithItemsDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.exception.ValidationException;
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

    private final BranchMenuCategoryRepository branchMenuCategoryRepository;
    private final BranchMenuItemRepository branchMenuItemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final BranchesRepository branchesRepository;
    private final BranchSecurityService branchSecurityService;
    private final MenuAuditService menuAuditService;
    private final MenuRealtimeService menuRealtimeService;

    private static final String UPLOAD_DIR = "uploads/branch-menu/";

    // ── Inheritance ───────────────────────────────────────────────────────────

    @Transactional
    public List<BranchMenuCategory> inheritRestaurantMenu(Long branchId) {
        log.info("Starting menu inheritance for branch: {}", branchId);

        Branches branch = branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        Restaurant restaurant = branch.getRestaurant();

        long existingMenuCount = branchMenuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount > 0) {
            throw new ValidationException("Branch already has menu items. Cannot inherit from restaurant.");
        }

        List<MenuCategory> restaurantCategories = menuCategoryRepository
                .findByRestaurantId(restaurant.getRestaurantId());

        List<BranchMenuCategory> inheritedCategories = new ArrayList<>();

        for (MenuCategory restaurantCategory : restaurantCategories) {
            BranchMenuCategory branchCategory = BranchMenuCategory.builder()
                    .categoryName(restaurantCategory.getCategoryName())
                    .isActive(restaurantCategory.getIsActive())
                    .branch(branch)
                    .createdAt(LocalDate.now())
                    .build();

            branchCategory = branchMenuCategoryRepository.save(branchCategory);

            List<BranchMenuItem> branchItems = new ArrayList<>();
            for (MenuItem restaurantItem : restaurantCategory.getMenuItems()) {
                BranchMenuItem branchItem = BranchMenuItem.builder()
                        .menuItemName(restaurantItem.getMenuItemName())
                        .description(restaurantItem.getDescription())
                        .price(restaurantItem.getPrice())
                        .image(restaurantItem.getImage())
                        .ingredients(restaurantItem.getIngredients())
                        .isAvailable(restaurantItem.isAvailable())
                        .preparationTime(restaurantItem.getPreparationTime())
                        .preparationScore(restaurantItem.getPreparationScore())
                        .sourceRestaurantItemId(restaurantItem.getMenuItemId())
                        .branch(branch)
                        .category(branchCategory)
                        .createdAt(LocalDate.now())
                        .updatedAt(LocalDate.now())
                        .build();

                for (MenuItemVariant restaurantVariant : restaurantItem.getVariants()) {
                    BranchMenuItemVariant branchVariant = BranchMenuItemVariant.builder()
                            .variantName(restaurantVariant.getVariantName())
                            .priceModifier(restaurantVariant.getPriceModifier())
                            .menuItem(branchItem)
                            .build();
                    branchItem.getVariants().add(branchVariant);
                }

                branchItems.add(branchItem);
            }

            branchMenuItemRepository.saveAll(branchItems);
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

    @Transactional
    public List<BranchMenuCategory> getBranchMenu(Long branchId) {
        if (branchMenuCategoryRepository.countByBranch_BranchId(branchId) == 0) {
            log.info("Branch {} has no menu, inheriting from restaurant", branchId);
            inheritRestaurantMenu(branchId);
        }
        return branchMenuCategoryRepository.findByBranch_BranchId(branchId);
    }

    @Transactional(readOnly = true)
    public MenuProgressiveResponseDTO getMenuProgressive(Long branchId, int page, int size, String categoryName) {
        long existingMenuCount = branchMenuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount == 0) {
            log.info("Branch {} has no menu, inheriting from restaurant first", branchId);
            inheritRestaurantMenu(branchId);
        }

        List<BranchMenuCategory> categories;
        if (categoryName != null && !categoryName.isEmpty()) {
            categories = branchMenuCategoryRepository
                    .findByBranch_BranchIdAndCategoryNameContainingIgnoreCase(branchId, categoryName);
        } else {
            categories = branchMenuCategoryRepository.findByBranch_BranchId(branchId);
        }

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, categories.size());
        List<BranchMenuCategory> paginatedCategories = categories.subList(startIndex, endIndex);

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

    @Transactional
    public List<MenuCategoryDTO> getBranchMenuCategories(Long branchId) {
        branchesRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        if (branchMenuCategoryRepository.countByBranch_BranchId(branchId) == 0) {
            log.info("Branch {} has no categories, inheriting from restaurant", branchId);
            inheritRestaurantMenu(branchId);
        }

        return branchMenuCategoryRepository.findByBranch_BranchId(branchId).stream()
                .map(category -> {
                    MenuCategoryDTO dto = new MenuCategoryDTO();
                    dto.setCategoryName(category.getCategoryName());
                    dto.setActive(category.getIsActive() != null && category.getIsActive());
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

        if (branchMenuCategoryRepository.existsByBranch_BranchIdAndCategoryName(branchId, categoryDTO.getCategoryName())) {
            throw new IllegalArgumentException("Category with this name already exists for this branch");
        }

        long existingMenuCount = branchMenuCategoryRepository.countByBranch_BranchId(branchId);
        if (existingMenuCount == 0) {
            log.info("Branch {} has no menu, inheriting from restaurant first", branchId);
            inheritRestaurantMenu(branchId);
        }

        BranchMenuCategory category = BranchMenuCategory.builder()
                .categoryName(categoryDTO.getCategoryName())
                .isActive(categoryDTO.isActive())
                .branch(branch)
                .createdAt(LocalDate.now())
                .build();

        BranchMenuCategory saved = branchMenuCategoryRepository.save(category);
        log.info("Created branch menu category '{}' for branch {}", categoryDTO.getCategoryName(), branchId);

        MenuCategoryResponseDTO response = new MenuCategoryResponseDTO();
        response.setCategoryId(saved.getCategoryId());
        response.setCategoryName(saved.getCategoryName());
        response.setBranchId(branchId);
        return response;
    }

    @Transactional
    public MenuCategoryResponseDTO updateMenuCategory(Long branchId, Long categoryId, MenuCategoryDTO categoryDTO) {
        verifyBranchAccess(branchId);

        BranchMenuCategory category = branchMenuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));

        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Category does not belong to this branch");
        }

        if (categoryDTO.getCategoryName() != null)
            category.setCategoryName(categoryDTO.getCategoryName());

        BranchMenuCategory updated = branchMenuCategoryRepository.save(category);
        log.info("Updated branch menu category {} for branch {}", categoryId, branchId);

        MenuCategoryResponseDTO response = new MenuCategoryResponseDTO();
        response.setCategoryId(updated.getCategoryId());
        response.setCategoryName(updated.getCategoryName());
        response.setBranchId(branchId);
        return response;
    }

    @Transactional
    public void deleteMenuCategory(Long branchId, Long categoryId) {
        BranchMenuCategory category = branchMenuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Category does not belong to this branch");
        }

        if (!category.getMenuItems().isEmpty()) {
            throw new ValidationException("Cannot delete category with existing items. Remove items first.");
        }

        branchMenuCategoryRepository.delete(category);
        log.info("Deleted branch menu category {} from branch {}", categoryId, branchId);
    }

    // ── Menu items ────────────────────────────────────────────────────────────

    @Transactional
    public List<MenuItemResponse> getBranchMenuItems(Long branchId, Long categoryId) {
        if (branchMenuCategoryRepository.countByBranch_BranchId(branchId) == 0) {
            log.info("Branch {} has no menu, inheriting from restaurant", branchId);
            inheritRestaurantMenu(branchId);
        }

        BranchMenuCategory category = branchMenuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));

        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Category does not belong to this branch");
        }

        return branchMenuItemRepository.findByCategory_CategoryId(categoryId).stream()
                .map(this::toMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponse createMenuItem(Long branchId, Long categoryId, MenuItemRequest menuItemRequest,
            MultipartFile imageFile) {
        verifyBranchAccess(branchId);

        BranchMenuCategory category = branchMenuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category not found"));

        if (!category.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Category does not belong to this branch");
        }

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        BranchMenuItem menuItem = BranchMenuItem.builder()
                .menuItemName(menuItemRequest.getMenuItemName())
                .description(menuItemRequest.getDescription())
                .price(menuItemRequest.getPrice())
                .image(imageUrl)
                .ingredients(menuItemRequest.getIngredients())
                .isAvailable(menuItemRequest.isAvailable())
                .preparationTime(menuItemRequest.getPreparationTime())
                .preparationScore(0)
                .branch(category.getBranch())
                .category(category)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        BranchMenuItem saved = branchMenuItemRepository.save(menuItem);
        log.info("Created branch menu item '{}' for category {} in branch {}",
                menuItemRequest.getMenuItemName(), categoryId, branchId);

        return toMenuItemResponse(saved);
    }

    @Transactional
    public BranchMenuItem updateMenuItem(Long branchId, Long menuItemId,
            UpdateMenuItemRequest updateRequest,
            MultipartFile imageFile,
            HttpServletRequest request) {
        BranchMenuItem menuItem = branchMenuItemRepository.findById(menuItemId)
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
        BranchMenuItem saved = branchMenuItemRepository.save(menuItem);

        menuRealtimeService.broadcastBranchMenuItemUpdate(branchId, saved, getCurrentUserEmail());

        return saved;
    }

    @Transactional
    public BranchMenuItem partialUpdateMenuItem(Long branchId, Long menuItemId,
            MenuItemPartialUpdateDTO partialUpdate,
            HttpServletRequest request) {
        BranchMenuItem menuItem = branchMenuItemRepository.findById(menuItemId)
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
        BranchMenuItem saved = branchMenuItemRepository.save(menuItem);

        if (fieldName != null && oldValue != null && newValue != null) {
            menuAuditService.logMenuItemUpdate(menuItemId, branchId, fieldName,
                    oldValue, newValue, "Auto-save", request);
        }

        menuRealtimeService.broadcastBranchMenuItemUpdate(branchId, saved, getCurrentUserEmail());

        return saved;
    }

    @Transactional
    public void deleteMenuItem(Long branchId, Long menuItemId, HttpServletRequest request) {
        BranchMenuItem menuItem = branchMenuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getBranch().getBranchId().equals(branchId)) {
            throw new ValidationException("Menu item does not belong to this branch");
        }

        menuAuditService.logMenuItemDelete(menuItemId, branchId, menuItem.getMenuItemName(), request);
        branchMenuItemRepository.delete(menuItem);
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

    public MenuItemResponse toMenuItemResponse(BranchMenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .description(item.getDescription())
                .price(item.getPrice())
                .image(item.getImage())
                .ingredients(item.getIngredients())
                .isAvailable(item.isAvailable())
                .preparationTime(item.getPreparationTime())
                .preparationScore(item.getPreparationScore())
                .categoryId(item.getCategory() != null ? item.getCategory().getCategoryId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getCategoryName() : null)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private MenuCategoryWithItemsDTO convertToCategoryWithItemsDTO(BranchMenuCategory category) {
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
                        .isInherited(item.getSourceRestaurantItemId() != null)
                        .build())
                .toList();

        return MenuCategoryWithItemsDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .items(itemDTOs)
                .isInherited(false)
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
