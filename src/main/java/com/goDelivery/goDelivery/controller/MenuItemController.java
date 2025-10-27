package com.goDelivery.goDelivery.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.UpdateMenuItemRequest;
import com.goDelivery.goDelivery.service.MenuItemService;
import com.goDelivery.goDelivery.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final FileStorageService fileStorageService;

    @PostMapping(value = "/createMenuItem/{restaurantId}", consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse createMenuItem(
            @PathVariable Long restaurantId,
            @RequestPart("menuItem") @Valid MenuItemRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            // Set the restaurant ID from path variable
            request.setRestaurantId(restaurantId);
            
            // Handle image upload if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(imageFile, "menu-items/temp/images");
                String fullUrl = "/api/files/" + filePath.replace("\\", "/");
                request.setImage(fullUrl);
            }
            
            return menuItemService.createMenuItem(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create menu item: " + e.getMessage(), e);
        }
    }

    @GetMapping(value = "/getMenuItemsByRestaurant/{restaurantId}")
    public List<MenuItemResponse> getMenuItemsByRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) Boolean available) {
        return menuItemService.getMenuItemsByRestaurantId(restaurantId).stream()
            .map(menuItemService::mapToResponse)
            .collect(Collectors.toList());
    }

    @GetMapping(value = "/getMenuItemById/{menuItemId}")
    public MenuItemResponse getMenuItemById(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        return menuItemService.getMenuItemById(menuItemId);
    }

    @GetMapping(value = "/getMenuItemByName/{menuItemName}")
    public MenuItemResponse getMenuItemByName(
            @PathVariable Long restaurantId,
            @PathVariable String menuItemName) {
        return menuItemService.getMenuItemByName(menuItemName);
    }

    @GetMapping(value = "/getAllMenuItem")
    public List<MenuItemResponse> getAllMenuItem() {
        return menuItemService.getAllMenuItems();
    }

    @PutMapping(value = "/updateMenuItem/{menuItemId}")
    public MenuItemResponse updateMenuItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        return menuItemService.updateMenuItem(menuItemId, request);
    }

    @DeleteMapping(value = "/deleteMenuItem/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        menuItemService.deleteMenuItem(menuItemId);
    }

    @PatchMapping(value = "/updateMenuItemAvailability/{menuItemId}")
    public MenuItemResponse updateMenuItemAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @RequestParam boolean available) {
        return menuItemService.updateMenuItemAvailability(menuItemId, available);
    }
}
