package com.goDelivery.goDelivery.modules.branch;

/**
 * Public API boundary for the Branch module.
 *
 * Exposed services:
 *   - BranchService      → branch CRUD, setup, activation
 *   - BranchMenuService  → branch menu management and inheritance
 *   - BranchUserService  → branch user management
 *
 * Exposed DTOs:
 *   - BranchesDTO / BranchCreationDTO
 *   - BranchUserDTO / BranchSettingsDTO
 *
 * Dependencies on other modules:
 *   - restaurant → Restaurant (parent of branch)
 *   - ordering   → Order (branch orders)
 */
public final class BranchModuleApi {
    private BranchModuleApi() {}
}
