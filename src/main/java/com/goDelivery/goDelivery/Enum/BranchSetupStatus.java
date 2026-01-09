package com.goDelivery.goDelivery.Enum;

public enum BranchSetupStatus {
    ACCOUNT_CREATED,          // Branch manager account created
    LOCATION_ADDED,           // Location details added
    SETTINGS_CONFIGURED,      // Settings configured (delivery, radius, etc.)
    OPERATING_HOURS_ADDED,    // Operating hours configured
    BRANDING_ADDED,           // Logo and branding added
    MENU_SETUP_STARTED,       // Started adding menu items
    MENU_SETUP_COMPLETED,     // Menu setup completed
    COMPLETED,                // Ready for operation
    ACTIVE,                   // Approved and active
    REJECTED,                 // Application rejected
    SUSPENDED                 // Branch suspended
}
