package com.goDelivery.goDelivery.modules.analytics;

/**
 * Public API boundary for the Analytics module.
 *
 * Exposed services:
 *   - AnalyticsService → order/customer analytics
 *   - ReportService    → sales reports, revenue summaries
 *
 * Exposed DTOs:
 *   - RestaurantDashboard / EnhancedRestaurantDashboard
 *   - SuperAdminDashboard / BikerDashboard
 *   - SalesReportDTO / OrderReportDTO
 *   - CustomerTrendsDTO / OrderAnalytics
 *
 * Dependencies on other modules:
 *   - ordering   → Order data
 *   - restaurant → Restaurant revenue
 *   - customer   → Customer trends
 *   - delivery   → Biker earnings
 */
public final class AnalyticsModuleApi {
    private AnalyticsModuleApi() {}
}
