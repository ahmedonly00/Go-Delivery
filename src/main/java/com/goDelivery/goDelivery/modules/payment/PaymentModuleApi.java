package com.goDelivery.goDelivery.modules.payment;

/**
 * Public API boundary for the Payment module.
 *
 * Exposed services:
 *   - PaymentService      → process payments, get customer payments
 *   - MomoService         → MoMo payment requests and status
 *   - MpesaPaymentService → M-PESA payment initiation and status
 *   - DisbursementService → disburse funds to restaurants/bikers
 *
 * Exposed DTOs:
 *   - PaymentRequest / PaymentResponse
 *   - MomoPaymentRequest / MomoPaymentResponse
 *   - MpesaPaymentRequest / MpesaPaymentResponse
 *   - DisbursementSummaryDTO / RestaurantDisbursementSummaryDTO
 *
 * Dependencies on other modules:
 *   - ordering  → Order entity (via OrderingModuleApi)
 *   - restaurant → Restaurant entity (via RestaurantModuleApi)
 *   - delivery  → Bikers entity (via DeliveryModuleApi)
 */
public final class PaymentModuleApi {
    private PaymentModuleApi() {}
}
