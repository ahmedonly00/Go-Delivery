package com.goDelivery.goDelivery.modules.ordering;

/**
 * Public API boundary for the Ordering module.
 *
 * Rules:
 * - Other modules MUST only depend on classes listed here.
 * - Never import directly from modules.ordering.service, .model, or .repository
 *   from outside this module — use the exposed service interfaces only.
 *
 * Exposed services (injectable by other modules):
 *   - OrderService    → com.goDelivery.goDelivery.modules.ordering.service.OrderService
 *   - CartService     → com.goDelivery.goDelivery.modules.ordering.service.CartService
 *
 * Exposed DTOs (safe to use across modules):
 *   - OrderRequest    → com.goDelivery.goDelivery.modules.ordering.dto.OrderRequest
 *   - OrderResponse   → com.goDelivery.goDelivery.modules.ordering.dto.OrderResponse
 *   - CartItemDTO     → com.goDelivery.goDelivery.modules.ordering.dto.CartItemDTO
 *   - ShoppingCartDTO → com.goDelivery.goDelivery.modules.ordering.dto.ShoppingCartDTO
 */
public final class OrderingModuleApi {
    private OrderingModuleApi() {}
}
