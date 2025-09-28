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
@RequestMapping("/api/restaurants/{restaurantId}/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        // Set the restaurant ID from path variable
        request.setRestaurantId(restaurantId);
        return menuItemService.createMenuItem(request);
    }

    @GetMapping
    public List<MenuItemResponse> getMenuItemsByRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) Boolean available) {
        return menuItemService.getMenuItemsByRestaurantId(restaurantId).stream()
            .map(menuItemService::mapToResponse)
            .collect(Collectors.toList());
    }

    @GetMapping("/{menuItemId}")
    public MenuItemResponse getMenuItemById(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        return menuItemService.getMenuItemById(menuItemId);
    }

    @PutMapping("/{menuItemId}")
    public MenuItemResponse updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        return menuItemService.updateMenuItem(menuItemId, request);
    }

    @DeleteMapping("/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId) {
        menuItemService.deleteMenuItem(menuItemId);
    }

    @PatchMapping("/{menuItemId}/availability")
    public MenuItemResponse updateMenuItemAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @RequestParam boolean available) {
        return menuItemService.updateMenuItemAvailability(menuItemId, available);
    }
}
