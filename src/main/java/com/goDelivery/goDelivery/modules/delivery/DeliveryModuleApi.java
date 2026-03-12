package com.goDelivery.goDelivery.modules.delivery;

/**
 * Public API boundary for the Delivery module.
 *
 * Exposed services:
 *   - BikerService                 → biker registration, order assignment
 *   - DeliveryTrackingService      → real-time tracking updates
 *   - DeliveryFeeCalculationService → fee calculation by distance/zone
 *   - GeocodingService             → address to coordinates
 *
 * Exposed DTOs:
 *   - DeliveryAcceptanceRequest / DeliveryAcceptanceResponse
 *   - DeliveryTrackingRequest / DeliveryTrackingResponse
 *   - BikerEarningsResponse
 *   - DeliveryFeeCalculationRequest / DeliveryFeeCalculationResponse
 *
 * Dependencies on other modules:
 *   - ordering → Order (delivery is triggered by an order)
 *   - customer → Customer (delivery destination)
 */
public final class DeliveryModuleApi {
    private DeliveryModuleApi() {}
}
