# Branch Management API Documentation

## Overview

The GoDelivery platform supports multi-branch restaurant management. This documentation covers all branch-related APIs, their endpoints, required permissions, and usage examples.

---

## Table of Contents

1. [Controllers Overview](#controllers-overview)
2. [Security & Permissions](#security--permissions)
3. [Branch Registration & Approval](#branch-registration--approval)
4. [Branch Management](#branch-management)
5. [Branch Setup Progress](#branch-setup-progress)
6. [Branch Menu Management](#branch-menu-management)
7. [Branch Menu Inheritance](#branch-menu-inheritance)
8. [Branch Orders & Payments](#branch-orders--payments)
9. [Branch Operations](#branch-operations)
10. [Branch Users](#branch-users)
11. [Branch File Upload](#branch-file-upload)
12. [Restaurant Admin Branch Management](#restaurant-admin-branch-management)

---

## Controllers Overview

| Controller | Base Path | Description |
|------------|-----------|-------------|
| `BranchController` | `/api/v1/branches` | Core branch CRUD operations |
| `BranchRegistrationController` | `/api/v1/branch-registration` | Branch registration and approval |
| `BranchSetupController` | `/api/v1/branch-setup` | Branch setup completion |
| `BranchSetupProgressController` | `/api/v1/branch-setup-progress` | Step-by-step setup management |
| `BranchMenuController` | `/api/v1/branches/{branchId}/menu` | Branch menu categories & items |
| `BranchMenuInheritanceController` | `/api/v1/branch-menu` | Menu inheritance from restaurant |
| `BranchOrderController` | `/api/v1/branch-orders` | Branch order management |
| `BranchOperationsController` | `/api/v1/branches/{branchId}/operations` | Branch-level operations |
| `BranchUserController` | `/api/v1/branch-users` | Branch user management |
| `BranchFileUploadController` | `/api/file-upload/branches` | Branch file uploads |
| `RestaurantAdminBranchController` | `/api/v1/restaurant-admin` | Restaurant admin branch operations |

---

## Security & Permissions

### Roles

| Role | Description | Permissions |
|------|-------------|-------------|
| `RESTAURANT_ADMIN` | Restaurant owner/administrator | Full access to all branches of their restaurant |
| `BRANCH_MANAGER` | Branch manager | Full access to their assigned branch only |

### Security Service

The `BranchSecurityService` validates access with these methods:
- `isRestaurantAdminOfBranch(username, branchId)` - Verifies restaurant admin owns the branch
- `isBranchManagerOfBranch(username, branchId)` - Verifies user is the branch manager

---

## Branch Registration & Approval

**Base Path:** `/api/v1/branch-registration`

### Register New Branch (Simple)

```
POST /api/v1/branch-registration/register
Content-Type: multipart/form-data
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| branchData | JSON String | Yes | Branch registration data |
| businessDocument | File | Yes | Business registration document |
| operatingLicense | File | Yes | Operating license document |

**Response:** `201 Created` with `BranchesDTO`

---

### Register Branch (Comprehensive)

```
POST /api/v1/branch-registration/register-comprehensive/{restaurantId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
Content-Type: multipart/form-data
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| branchData | BranchCreationDTO | Yes | Branch creation details |
| logo | File | No | Branch logo image |
| documents | File[] | No | Supporting documents |

**Response:** `201 Created` with `BranchesDTO`

---

### Approve Branch

```
PUT /api/v1/branch-registration/{branchId}/approve
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

**Response:** `200 OK` with `BranchesDTO`

---

### Reject Branch

```
PUT /api/v1/branch-registration/{branchId}/reject?rejectionReason={reason}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| rejectionReason | String | Yes | Reason for rejection |

**Response:** `200 OK` with `BranchesDTO`

---

### Get Pending Branches

```
GET /api/v1/branch-registration/pending
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

**Response:** `200 OK` with list of pending branches

---

## Branch Management

**Base Path:** `/api/v1/branches`

### Create Branch

```
POST /api/v1/branches/create/{restaurantId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
Content-Type: application/json
```

**Request Body:** `BranchCreationDTO`
```json
{
  "branchName": "Downtown Branch",
  "address": "123 Main Street",
  "latitude": -1.9403,
  "longitude": 29.8739,
  "phoneNumber": "+250788123456",
  "email": "downtown@restaurant.com"
}
```

**Response:** `200 OK` with `BranchesDTO`

---

### Update Branch

```
PUT /api/v1/branches/update/{branchId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
Content-Type: multipart/form-data
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| branchData | BranchCreationDTO | Yes | Updated branch data |
| logo | File | No | New branch logo |

**Response:** `200 OK` with `BranchesDTO`

---

### Get Restaurant Branches

```
GET /api/v1/branches/restaurant/{restaurantId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

**Response:** `200 OK` with `List<BranchesDTO>`

---

### Get Branch Details

```
GET /api/v1/branches/{branchId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

**Response:** `200 OK` with `BranchesDTO`

---

### Activate Branch

```
POST /api/v1/branches/{branchId}/activate
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

**Response:** `200 OK`

---

### Deactivate Branch

```
POST /api/v1/branches/{branchId}/deactivate
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

**Response:** `200 OK`

---

## Branch Setup Progress

**Base Path:** `/api/v1/branch-setup-progress`

### Get Setup Status

```
GET /api/v1/branch-setup-progress/{branchId}/status
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

**Response:** `200 OK` with `BranchesDTO`

---

### Update Setup Status

```
PUT /api/v1/branch-setup-progress/{branchId}/status?status={status}
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

**Status Values:** `PENDING`, `IN_PROGRESS`, `COMPLETED`, `APPROVED`

---

### Add Location Details

```
PUT /api/v1/branch-setup-progress/{branchId}/location
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| address | String | Yes | Branch address |
| latitude | Float | Yes | GPS latitude |
| longitude | Float | Yes | GPS longitude |

---

### Configure Delivery Settings

```
PUT /api/v1/branch-setup-progress/{branchId}/delivery-settings
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| deliveryType | DeliveryType | Yes | DELIVERY, PICKUP, BOTH |
| deliveryFee | Float | No | Delivery fee amount |
| deliveryRadius | Double | No | Delivery radius in km |
| minimumOrderAmount | Float | No | Minimum order amount |
| averagePrepTime | Integer | No | Average prep time (minutes) |

---

### Add Operating Hours

```
POST /api/v1/branch-setup-progress/{branchId}/operating-hours
Authorization: Bearer <token>
Role: BRANCH_MANAGER
Content-Type: application/json
```

**Request Body:** `OperatingHours`

---

### Add Branding

```
PUT /api/v1/branch-setup-progress/{branchId}/branding
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| logoUrl | String | No | URL to branch logo |
| description | String | No | Branch description |

---

### Menu Setup Flow

```
PUT /api/v1/branch-setup-progress/{branchId}/menu-setup/start
PUT /api/v1/branch-setup-progress/{branchId}/menu-setup/complete
```

---

### Complete Branch Setup

```
PUT /api/v1/branch-setup-progress/{branchId}/complete
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

---

## Branch Menu Management

**Base Path:** `/api/v1/branches/{branchId}/menu`

### Create Menu Category

```
POST /api/v1/branches/{branchId}/menu/categories
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
Content-Type: application/json
```

**Request Body:** `MenuCategoryDTO`
```json
{
  "categoryName": "Main Dishes",
  "description": "Our signature main courses"
}
```

---

### Create Menu Item

```
POST /api/v1/branches/{branchId}/menu/categories/{categoryId}/items
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
Content-Type: multipart/form-data
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| itemData | MenuItemRequest | Yes | Menu item details |
| image | File | No | Menu item image |

---

### Get Menu Categories

```
GET /api/v1/branches/{branchId}/menu/categories
```

**Response:** `200 OK` with `List<MenuCategoryDTO>`

---

### Get Menu Items

```
GET /api/v1/branches/{branchId}/menu/categories/{categoryId}/items
```

**Response:** `200 OK` with `List<MenuItemResponse>`

---

### Update Menu Category

```
PUT /api/v1/branches/{branchId}/menu/categories/{categoryId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Delete Menu Category

```
DELETE /api/v1/branches/{branchId}/menu/categories/{categoryId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

## Branch Menu Inheritance

**Base Path:** `/api/v1/branch-menu`

### Inherit Restaurant Menu

```
POST /api/v1/branch-menu/{branchId}/inherit
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

Copies all menu categories and items from the parent restaurant to the branch.

**Response:** `200 OK` with `List<MenuCategory>`

---

### Get Branch Menu

```
GET /api/v1/branch-menu/{branchId}
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

---

### Get Menu Progressive (Paginated)

```
GET /api/v1/branch-menu/{branchId}/progressive?page=0&size=10&categoryName={name}
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

---

### Add Menu Category

```
POST /api/v1/branch-menu/{branchId}/categories
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

---

### Add Menu Item

```
POST /api/v1/branch-menu/{branchId}/categories/{categoryId}/items
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

---

### Update Menu Item

```
PUT /api/v1/branch-menu/{branchId}/items/{menuItemId}
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

---

### Auto-save Menu Item (Partial Update)

```
PATCH /api/v1/branch-menu/{branchId}/items/{menuItemId}/autosave
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

---

### Delete Menu Category

```
DELETE /api/v1/branch-menu/{branchId}/categories/{categoryId}
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

---

### Delete Menu Item

```
DELETE /api/v1/branch-menu/{branchId}/items/{menuItemId}
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

---

## Branch Orders & Payments

**Base Path:** `/api/v1/branch-orders`

### Get Branch Orders

```
GET /api/v1/branch-orders/branch/{branchId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Get Pending Orders

```
GET /api/v1/branch-orders/branch/{branchId}/pending
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Update Order Status

```
PUT /api/v1/branch-orders/branch/{branchId}/update-status/{orderId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
Content-Type: application/json
```

**Request Body:** `OrderStatusUpdate`

---

### Accept Order

```
POST /api/v1/branch-orders/branch/{branchId}/accept-order/{orderId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

Sets order status to `PREPARING`

---

### Mark Order Ready

```
POST /api/v1/branch-orders/branch/{branchId}/ready-for-pickup/{orderId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

Sets order status to `READY`

---

### Complete Order

```
POST /api/v1/branch-orders/branch/{branchId}/complete-order/{orderId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

Sets order status to `DELIVERED`

---

### Cancel Order

```
POST /api/v1/branch-orders/branch/{branchId}/cancel-order/{orderId}?cancellationReason={reason}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Get Branch Payments

```
GET /api/v1/branch-orders/branch/{branchId}/payments
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Get Payment Details

```
GET /api/v1/branch-orders/branch/{branchId}/payment/{paymentId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Process Refund

```
POST /api/v1/branch-orders/branch/{branchId}/refund/{paymentId}?refundReason={reason}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Get Branch Statistics

```
GET /api/v1/branch-orders/branch/{branchId}/stats
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

**Response:**
```json
{
  "totalOrders": 150,
  "completedOrders": 120,
  "cancelledOrders": 10,
  "pendingOrders": 20
}
```

---

## Branch Operations

**Base Path:** `/api/v1/branches/{branchId}/operations`

### Get Branch Details

```
GET /api/v1/branches/{branchId}/operations
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER (with ownership verification)
```

---

### Update Branch Details

```
PUT /api/v1/branches/{branchId}/operations
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER (with ownership verification)
```

---

### Get Branch Menu

```
GET /api/v1/branches/{branchId}/operations/menu
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Add Menu Item

```
POST /api/v1/branches/{branchId}/operations/menu/items
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Update Menu Item

```
PUT /api/v1/branches/{branchId}/operations/menu/items/{menuItemId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Create Order for Branch

```
POST /api/v1/branches/{branchId}/operations/orders
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Get Branch Orders

```
GET /api/v1/branches/{branchId}/operations/orders?status={status}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Create Branch User

```
POST /api/v1/branches/{branchId}/operations/users
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN only
```

---

### Get Branch Users

```
GET /api/v1/branches/{branchId}/operations/users
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

### Get Branch Analytics

```
GET /api/v1/branches/{branchId}/operations/analytics?reportType={type}&startDate={date}&endDate={date}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN only
```

---

### Get/Update Branch Settings

```
GET /api/v1/branches/{branchId}/operations/settings
PUT /api/v1/branches/{branchId}/operations/settings
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
```

---

## Branch Users

**Base Path:** `/api/v1/branch-users`

### Get My Branch (Branch Manager)

```
GET /api/v1/branch-users/my-branch
Authorization: Bearer <token>
Role: BRANCH_MANAGER
```

---

### Create Branch User

```
POST /api/v1/branch-users/branch/{branchId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
Content-Type: application/json
```

**Request Body:** `BranchUserDTO`
```json
{
  "email": "manager@branch.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+250788123456",
  "role": "BRANCH_MANAGER"
}
```

---

### Get Branch Users

```
GET /api/v1/branch-users/branch/{branchId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

---

### Get Active Branch Users

```
GET /api/v1/branch-users/branch/{branchId}/active
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

---

### Update Branch User

```
PUT /api/v1/branch-users/{userId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

---

### Toggle User Status

```
PUT /api/v1/branch-users/{userId}/status?isActive={true|false}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

---

### Register Branch Manager (Public)

```
POST /api/v1/branch-users/register
Content-Type: application/json
```

Returns authentication token after registration.

---

## Branch File Upload

**Base Path:** `/api/file-upload/branches`

### Upload Branch Menu

```
POST /api/file-upload/branches/{branchId}/menu-upload
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN or BRANCH_MANAGER
Content-Type: multipart/form-data
```

**Parameters:**
| Name | Type | Required | Description |
|------|------|----------|-------------|
| file | File | Yes | Menu file (PDF, Excel, Image) - Max 10MB |

The file is processed using OCR to extract menu items automatically.

**Response:** `FileUploadResponse`
```json
{
  "success": true,
  "message": "Menu processed successfully",
  "menuItems": [
    {
      "name": "Grilled Chicken",
      "price": 5000,
      "category": "Main Dishes"
    }
  ]
}
```

---

## Restaurant Admin Branch Management

**Base Path:** `/api/v1/restaurant-admin`

### Get All Restaurant Branches

```
GET /api/v1/restaurant-admin/branches
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

---

### Get Branch Analytics

```
GET /api/v1/restaurant-admin/branches/{branchId}/analytics
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

**Parameters:**
| Name | Type | Default | Description |
|------|------|---------|-------------|
| reportType | String | sales | Type: sales, orders, customers |
| startDate | String | -30 days | Start date (yyyy-MM-dd) |
| endDate | String | today | End date (yyyy-MM-dd) |

---

### Get Restaurant-wide Analytics

```
GET /api/v1/restaurant-admin/analytics
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

**Parameters:**
| Name | Type | Default | Description |
|------|------|---------|-------------|
| reportType | String | sales | Type: sales, orders, customers, performance |
| startDate | String | -30 days | Start date (yyyy-MM-dd) |
| endDate | String | today | End date (yyyy-MM-dd) |

---

### Add Menu Item to Branch

```
POST /api/v1/restaurant-admin/branches/{branchId}/menu-items/{menuItemId}
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

Copies a restaurant menu item to a specific branch.

---

### Compare Branches Performance

```
GET /api/v1/restaurant-admin/branches-comparison
Authorization: Bearer <token>
Role: RESTAURANT_ADMIN
```

**Parameters:**
| Name | Type | Default | Description |
|------|------|---------|-------------|
| metric | String | revenue | Metric: revenue, orders, rating |
| period | String | monthly | Period: daily, weekly, monthly |

---

## Branch Setup Flow

### Recommended Setup Sequence

1. **Create Branch** - Restaurant admin creates basic branch
2. **Add Location** - Set address and GPS coordinates
3. **Configure Delivery** - Set delivery type, fees, radius
4. **Add Operating Hours** - Set business hours
5. **Add Branding** - Upload logo and description
6. **Setup Menu** - Either inherit from restaurant or create new
7. **Complete Setup** - Mark setup as complete
8. **Approve Branch** - Restaurant admin approves
9. **Activate Branch** - Make branch live

```
POST /api/v1/branches/create/{restaurantId}
     ↓
PUT /api/v1/branch-setup-progress/{branchId}/location
     ↓
PUT /api/v1/branch-setup-progress/{branchId}/delivery-settings
     ↓
POST /api/v1/branch-setup-progress/{branchId}/operating-hours
     ↓
PUT /api/v1/branch-setup-progress/{branchId}/branding
     ↓
POST /api/v1/branch-menu/{branchId}/inherit  (or manual menu setup)
     ↓
PUT /api/v1/branch-setup-progress/{branchId}/complete
     ↓
PUT /api/v1/branch-registration/{branchId}/approve
     ↓
POST /api/v1/branches/{branchId}/activate
```

---

## Error Responses

All endpoints return standard error responses:

```json
{
  "timestamp": "2026-01-14T09:00:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/branches/create/1"
}
```

### Common HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Missing/invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource doesn't exist |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error |

---

## DTOs Reference

### BranchCreationDTO

```json
{
  "branchName": "string",
  "address": "string",
  "latitude": 0.0,
  "longitude": 0.0,
  "phoneNumber": "string",
  "email": "string",
  "description": "string"
}
```

### BranchesDTO

```json
{
  "branchId": 1,
  "branchName": "string",
  "address": "string",
  "latitude": 0.0,
  "longitude": 0.0,
  "phoneNumber": "string",
  "email": "string",
  "logoUrl": "string",
  "isActive": true,
  "setupStatus": "COMPLETED",
  "createdAt": "2026-01-14",
  "restaurant": {...}
}
```

### BranchUserDTO

```json
{
  "userId": 1,
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "string",
  "role": "BRANCH_MANAGER",
  "branchId": 1,
  "isActive": true
}
```

---

*Documentation generated: January 14, 2026*
*Version: 1.0*
