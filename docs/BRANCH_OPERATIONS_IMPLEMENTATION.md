# Branch Operations Implementation

This implementation extends your existing restaurant system to support full branch functionality while keeping Restaurant as the primary operational unit.

## Architecture Overview

```
Restaurant (Primary Operational Unit)
├── RestaurantAdmin (manages restaurant and all branches)
├── Restaurant-level features (brand settings, analytics)
└── Branches (Extended operational units)
    ├── BranchManager (manages specific branch)
    ├── Inherits restaurant capabilities
    └── Branch-specific overrides
```

## Key Components

### 1. BranchSecurityService
- Handles access control for branch operations
- Validates if users can access specific branches
- Supports both RestaurantAdmin and BranchManager roles
- Methods:
  - `canAccessBranch(branchId, role)` - Check general access
  - `isRestaurantAdminOfBranch(username, branchId)` - Restaurant admin validation
  - `isBranchManagerOfBranch(username, branchId)` - Branch manager validation
  - `canManageBranchUsers(branchId)` - User management permissions

### 2. BranchDelegationService
- Delegates restaurant operations to branch level
- Merges restaurant menu with branch-specific overrides
- Handles branch-specific orders and users
- Key methods:
  - `getBranchMenu(branchId)` - Gets merged menu
  - `createOrderForBranch(request, branchId)` - Creates branch order
  - `createBranchUser(branchId, userDTO)` - Creates branch user
  - `getBranchOrders(branchId, status)` - Gets branch orders

### 3. BranchOperationsController
- REST endpoints for branch operations
- Supports both RestaurantAdmin and BranchManager access
- Endpoints include:
  - GET/PUT `/api/v1/branches/{branchId}` - Branch details
  - GET `/api/v1/branches/{branchId}/menu` - Branch menu
  - POST `/api/v1/branches/{branchId}/menu/items` - Add menu item
  - POST `/api/v1/branches/{branchId}/orders` - Create order
  - GET `/api/v1/branches/{branchId}/orders` - Get orders
  - POST `/api/v1/branches/{branchId}/users` - Create user (Restaurant Admin only)

### 4. BranchEnabledRestaurantService
- Extends restaurant service to work with branches
- Handles branch-specific order creation
- Provides analytics for both branch and restaurant levels
- Key methods:
  - `createOrderForBranch(request, branchId)` - Order with branch context
  - `getOrdersByBranch(branchId, status)` - Filtered orders
  - `getRestaurantBranches(restaurantId)` - All restaurant branches
  - `getBranchAnalytics(branchId, ...)` - Branch analytics
  - `getRestaurantAnalytics(restaurantId, ...)` - Restaurant-wide analytics

### 5. RestaurantAdminBranchController
- Additional endpoints specifically for restaurant admins
- Branch management and analytics
- Endpoints:
  - GET `/api/v1/restaurant-admin/branches` - All branches
  - GET `/api/v1/restaurant-admin/branches/{branchId}/analytics` - Branch analytics
  - GET `/api/v1/restaurant-admin/analytics` - Restaurant analytics
  - POST `/api/v1/restaurant-admin/branches/{branchId}/menu-items/{menuItemId}` - Add menu to branch
  - GET `/api/v1/restaurant-admin/branches-comparison` - Compare branches

## Security Implementation

### Access Control Pattern
```java
// Restaurant Admin can manage all their branches
@PreAuthorize("hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)")

// Branch Manager can only manage their branch
@PreAuthorize("hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId)")

// Both can access (with different permissions)
@PreAuthorize("(hasRole('RESTAURANT_ADMIN') and @branchSecurity.isRestaurantAdminOfBranch(authentication.name, #branchId)) or " +
              "(hasRole('BRANCH_MANAGER') and @branchSecurity.isBranchManagerOfBranch(authentication.name, #branchId))")
```

## Database Schema

Your existing entities already support branches:
- `Order` has nullable `branch_id` field
- `MenuItem` has nullable `branch_id` field
- `Branches` entity linked to `Restaurant`

## API Usage Examples

### Restaurant Admin Managing Branch
```bash
# Get all branches
GET /api/v1/restaurant-admin/branches
Authorization: Bearer {restaurant_admin_token}

# Get branch menu (includes restaurant menu + branch overrides)
GET /api/v1/branches/{branchId}/menu
Authorization: Bearer {restaurant_admin_token}

# Create branch user
POST /api/v1/branches/{branchId}/users
{
  "fullName": "John Doe",
  "email": "john@branch.com",
  "password": "password123",
  "role": "BRANCH_MANAGER"
}
Authorization: Bearer {restaurant_admin_token}

# Get branch analytics
GET /api/v1/restaurant-admin/branches/{branchId}/analytics?reportType=sales&startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {restaurant_admin_token}
```

### Branch Manager Operations
```bash
# Get branch details
GET /api/v1/branches/{branchId}
Authorization: Bearer {branch_manager_token}

# Update menu item
PUT /api/v1/branches/{branchId}/menu/items/{menuItemId}
{
  "menuItemName": "Updated Item",
  "price": 15.99,
  "isAvailable": true
}
Authorization: Bearer {branch_manager_token}

# Create order
POST /api/v1/branches/{branchId}/orders
{
  "customerDetails": {...},
  "orderItems": [...],
  "deliveryAddress": "123 Main St"
}
Authorization: Bearer {branch_manager_token}
```

## Benefits

1. **No Breaking Changes**: Existing restaurant functionality continues to work
2. **Incremental Implementation**: Can be implemented gradually
3. **Flexible Access**: Restaurant admin can access all branches, branch manager only their own
4. **Menu Inheritance**: Branches inherit restaurant menu with ability to override
5. **Unified Operations**: All operations work at branch level while maintaining restaurant context
6. **Analytics**: Both branch-level and restaurant-wide analytics available

## Migration Steps

1. Deploy the new services and controllers
2. Update frontend to use branch-specific endpoints
3. Migrate existing orders to have branch references if needed
4. Test with both user roles
5. Gradually move operations to branch-level endpoints

## Notes

- All existing restaurant endpoints remain functional
- Branch-specific endpoints provide enhanced functionality
- Security is handled at method level for fine-grained control
- Analytics can be implemented based on your existing reporting system
- The implementation is backward compatible with single-branch restaurants
