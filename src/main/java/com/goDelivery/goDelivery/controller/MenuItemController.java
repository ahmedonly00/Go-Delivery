package com.goDelivery.goDelivery.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.goDelivery.goDelivery.dtos.menu.MenuItemResponse;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.dtos.menu.UpdateMenuItemRequest;
import com.goDelivery.goDelivery.service.MenuItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping(value = "/createMenuItem/{restaurantId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        // Set the restaurant ID from path variable
        request.setRestaurantId(restaurantId);
        return menuItemService.createMenuItem(request);
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

    @GetMapping(value = "/getMenuItemByName/{menuItemId}")
    public List<MenuItemResponse> getAllMenuItem() {
        return menuItemService.getAllMenuItems();
    }

    @PutMapping(value = "/updateMenuItem/{menuItemId}")
    public MenuItemResponse updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        return menuItemService.updateMenuItem(menuItemId, request);
    }

    @DeleteMapping(value = "/deleteMenuItem/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        menuItemService.deleteMenuItem(menuItemId);
    }

    @PatchMapping(value = "/updateMenuItemAvailability/{menuItemId}")
    public MenuItemResponse updateMenuItemAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @RequestParam boolean available) {
        return menuItemService.updateMenuItemAvailability(menuItemId, available);
    }
}
