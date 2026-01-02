# Branch Registration and Management System

## Overview
The branch registration system allows restaurant admins to create branches that can operate independently with their own menu, staff, and operations while being connected to the parent restaurant.

## Features

### 1. Branch Registration
- **Public Registration Endpoint**: `/api/v1/branch-registration/register`
  - Allows submission of branch registration with:
    - Branch details (name, address, contact, operating hours)
    - Business documents (registration, license)
    - Branch manager account creation
    - Initial menu categories setup
  - Status: PENDING approval by default

### 2. Branch Approval Workflow
- **Approve Branch**: `/api/v1/branch-registration/{branchId}/approve`
  - Restaurant admin can approve pending branches
  - Branch becomes active after approval
- **Reject Branch**: `/api/v1/branch-registration/{branchId}/reject`
  - Restaurant admin can reject with reason
- **View Pending**: `/api/v1/branch-registration/pending`
  - View all pending branch registrations

### 3. Branch User Management
- **Create Branch Users**: `/api/v1/branch-users/branch/{branchId}`
- **Register Branch Manager**: `/api/v1/branch-users/register` (public)
- **Update User**: `/api/v1/branch-users/{userId}`
- **Toggle Status**: `/api/v1/branch-users/{userId}/status`

### 4. Branch Menu Management
- **Create Menu Category**: `/api/v1/branches/{branchId}/menu/categories`
- **Create Menu Item**: `/api/v1/branches/{branchId}/menu/categories/{categoryId}/items`
- **View Menu**: `/api/v1/branches/{branchId}/menu/categories`
- **Update/Delete**: Full CRUD operations for categories and items

### 5. Branch Operations
Branch managers can perform all restaurant-level operations but limited to their branch:
- **Order Management**: View, update status of branch orders
- **Menu Management**: Create/update menu items and categories
- **Staff Management**: Create and manage branch users
- **Analytics**: View branch-specific analytics

## API Endpoints Summary

### Branch Registration
```
POST /api/v1/branch-registration/register
PUT /api/v1/branch-registration/{branchId}/approve
PUT /api/v1/branch-registration/{branchId}/reject
GET /api/v1/branch-registration/pending
```

### Branch Management
```
POST /api/v1/branches/addBranch/{restaurantId}
GET /api/v1/branches/my-branches
GET /api/v1/branches/my-branches/{branchId}
PUT /api/v1/branches/updateBranch/{branchId}
PUT /api/v1/branches/{branchId}/status
DELETE /api/v1/branches/deleteBranch/{branchId}
```

### Branch Users
```
POST /api/v1/branch-users/register
POST /api/v1/branch-users/branch/{branchId}
GET /api/v1/branch-users/branch/{branchId}
PUT /api/v1/branch-users/{userId}
PUT /api/v1/branch-users/{userId}/status
```

### Branch Menu
```
POST /api/v1/branches/{branchId}/menu/categories
POST /api/v1/branches/{branchId}/menu/categories/{categoryId}/items
GET /api/v1/branches/{branchId}/menu/categories
GET /api/v1/branches/{branchId}/menu/categories/{categoryId}/items
PUT /api/v1/branches/{branchId}/menu/categories/{categoryId}
DELETE /api/v1/branches/{branchId}/menu/categories/{categoryId}
```

### Branch Orders
```
GET /api/orders/branch/{branchId}
GET /api/orders/branch/{branchId}/total
```

## Security and Permissions

### Roles
- **RESTAURANT_ADMIN**: Can manage all branches and their operations
- **BRANCH_MANAGER**: Can manage only their assigned branch
- **BRANCH_STAFF**: Limited permissions within branch

### Access Control
- Restaurant admins can access all branches of their restaurant
- Branch managers can only access their assigned branch
- Document uploads are validated and stored securely
- All operations are logged for audit purposes

## Database Schema Updates

### Branches Table
- Added approval status fields
- Added document URLs
- Added description and review fields

### BranchUsers Table
- New table for branch-specific users
- Links to both Restaurant and Branch entities

### MenuCategory & MenuItem
- Updated to support both restaurant and branch contexts
- Branches can have independent menus

## File Upload Structure
```
uploads/
├── branch-docs/          # Business documents
├── branch-menu/          # Menu item images
└── restaurants/          # Existing restaurant uploads
```

## Implementation Notes

1. **Approval Workflow**: All new branches require approval before becoming active
2. **Independent Operations**: Branches operate independently but report to parent restaurant
3. **Menu Independence**: Each branch can have its own menu items and pricing
4. **User Management**: Branch users are separate from restaurant users
5. **Document Management**: Secure upload and storage of business documents

## Testing Considerations

1. Test branch registration with valid documents
2. Verify approval/rejection workflow
3. Test branch manager permissions
4. Verify menu management at branch level
5. Test order operations for branches
6. Verify security access controls

## Future Enhancements

1. Branch-specific promotions and discounts
2. Inventory management per branch
3. Branch performance analytics dashboard
4. Multi-branch order routing
5. Branch transfer functionality
