package com.goDelivery.goDelivery.Enum;

public enum RestaurantSetupStatus {
    ACCOUNT_CREATED,          // User account created, email not verified
    EMAIL_VERIFIED,           // Email verified, setup not started
    BASIC_INFO_ADDED,         // Basic restaurant info added
    LOCATION_ADDED,           // Location details added
    SETTINGS_CONFIGURED,      // Settings configured
    OPERATING_HOURS_ADDED,    // Operating hours configured
    BRANDING_ADDED,           // Logo and branding added
    MENU_SETUP_STARTED,       // Started adding menu items
    MENU_SETUP_COMPLETED,     // Menu setup completed
    COMPLETED,                // Ready for admin review (optional)
    ACTIVE,                   // Approved and active
    REJECTED,                 // Application rejected
    SUSPENDED                 // Account suspended
}
