package com.goDelivery.goDelivery.modules.restaurant;

/**
 * Public API boundary for the Restaurant module.
 *
 * Exposed services:
 *   - RestaurantService    → restaurant CRUD, approval workflow
 *   - MenuItemService      → menu item management
 *   - MenuCategoryService  → category management
 *   - PromotionService     → promotion management
 *
 * Exposed DTOs:
 *   - RestaurantResponse / RestaurantDTO
 *   - MenuItemResponse / MenuItemRequest
 *   - MenuCategoryDTO / MenuCategoryResponseDTO
 *   - PromotionRequest / PromotionResponse
 *
 * Dependencies on other modules:
 *   - branch → Branches (branch is a sub-domain of restaurant)
 */
public final class RestaurantModuleApi {
    private RestaurantModuleApi() {}
}
