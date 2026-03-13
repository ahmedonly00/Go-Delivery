package com.goDelivery.goDelivery.modules.customer;

/**
 * Public API boundary for the Customer module.
 *
 * Exposed services:
 *   - CustomerService  → customer registration, profile management
 *   - ReviewService    → review submission and moderation
 *   - FeedbackService  → feedback collection
 *
 * Exposed DTOs:
 *   - CustomerResponse / CustomerRegistrationRequest
 *   - ReviewRequest / ReviewResponse
 *   - FeedbackRequest / FeedbackResponse
 *   - WalletResponse / WalletTransactionResponse
 *
 * Dependencies on other modules:
 *   - ordering  → Order (customer order history)
 *   - restaurant → Restaurant (restaurant being reviewed)
 */
public final class CustomerModuleApi {
    private CustomerModuleApi() {}
}
