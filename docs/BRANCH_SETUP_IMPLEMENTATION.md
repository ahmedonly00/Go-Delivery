# Branch Setup Implementation

## Overview
Implemented a comprehensive branch setup system similar to restaurant setup, with step-by-step configuration tracking and progress management.

## Key Components Added

### 1. BranchSetupStatus Enum
Tracks the setup progress with the following stages:
- `ACCOUNT_CREATED` - Initial state after branch creation
- `LOCATION_ADDED` - Location details configured
- `SETTINGS_CONFIGURED` - Delivery and operational settings
- `OPERATING_HOURS_ADDED` - Business hours set
- `BRANDING_ADDED` - Logo and description added
- `MENU_SETUP_STARTED` - Menu creation began
- `MENU_SETUP_COMPLETED` - Menu setup finished
- `COMPLETED` - Ready for operation
- `ACTIVE` - Approved and operational
- `REJECTED` - Application rejected
- `SUSPENDED` - Branch suspended

### 2. Enhanced Branches Model
Added missing fields to match restaurant functionality:
- `setupStatus` - Tracks setup progress
- `deliveryType` - System/self/no delivery options
- `averagePreparationTime` - Food preparation time
- `deliveryFee`, `deliveryRadius`, `minimumOrderAmount` - Delivery configuration
- `businessDocumentUrl`, `operatingLicenseUrl` - Required documents
- `deliveryAvailable` - Boolean flag for delivery availability

### 3. BranchSetupProgressService
Manages the step-by-step setup process:
- Update setup status
- Add location details
- Configure delivery settings
- Add operating hours
- Add branding elements
- Manage menu setup progress

### 4. BranchSetupProgressController
REST endpoints for branch setup:
```
PUT /api/v1/branch-setup-progress/{branchId}/status
PUT /api/v1/branch-setup-progress/{branchId}/location
PUT /api/v1/branch-setup-progress/{branchId}/delivery-settings
POST /api/v1/branch-setup-progress/{branchId}/operating-hours
PUT /api/v1/branch-setup-progress/{branchId}/branding
PUT /api/v1/branch-setup-progress/{branchId}/menu-setup/start
PUT /api/v1/branch-setup-progress/{branchId}/menu-setup/complete
PUT /api/v1/branch-setup-progress/{branchId}/complete
```

### 5. Updated Models and DTOs
- **BranchesDTO** - Added setupStatus, deliveryType, averagePreparationTime
- **RestaurantMapper** - Updated mapping methods for new fields
- **OperatingHours** - Added branch field support
- **BranchSetupDTO** - Enhanced with more configuration options

## Branch Setup Flow

### 1. Branch Creation (Restaurant Admin)
- Creates branch with basic information
- Creates branch manager account
- Sets initial status to `ACCOUNT_CREATED`
- Branch remains inactive until approval and setup

### 2. Branch Manager Login
- Uses provided credentials to log in
- Receives JWT token with `BRANCH_MANAGER` role

### 3. Step-by-Step Setup
The branch manager can complete setup in any order:

#### Location Setup
```json
PUT /api/v1/branch-setup-progress/{branchId}/location
{
  "address": "123 Main St, City",
  "latitude": 40.7128,
  "longitude": -74.0060
}
```

#### Delivery Configuration
```json
PUT /api/v1/branch-setup-progress/{branchId}/delivery-settings
{
  "deliveryType": "SELF_DELIVERY",
  "deliveryFee": 5.99,
  "deliveryRadius": 10.0,
  "minimumOrderAmount": 20.0,
  "averagePrepTime": 30
}
```

#### Operating Hours
```json
POST /api/v1/branch-setup-progress/{branchId}/operating-hours
{
  "mondayOpen": "09:00",
  "mondayClose": "22:00",
  "tuesdayOpen": "09:00",
  "tuesdayClose": "22:00",
  // ... etc for all days
}
```

#### Branding
```json
PUT /api/v1/branch-setup-progress/{branchId}/branding
{
  "logoUrl": "/uploads/branches/logo.jpg",
  "description": "Our downtown location serving fresh food"
}
```

#### Menu Setup
- Start menu setup: `PUT /menu-setup/start`
- Create categories and items via BranchMenuController
- Complete menu setup: `PUT /menu-setup/complete`

### 4. Completion
- Mark setup as complete: `PUT /complete`
- Branch becomes active if approved
- Branch manager can start managing operations

## Security and Access Control

- All endpoints require `BRANCH_MANAGER` role
- Branch managers can only access their own branch
- Restaurant admins can view all branches
- Proper validation at each step

## Benefits

1. **Structured Process** - Clear setup progression
2. **Flexibility** - Complete steps in any order
3. **Tracking** - Monitor setup progress
4. **Consistency** - Similar to restaurant setup
5. **Validation** - Each step validated before completion

## API Examples

### Check Setup Status
```http
GET /api/v1/branch-setup/{branchId}/status
Authorization: Bearer {token}
```

### Complete Setup in One Go
```http
POST /api/v1/branch-setup/{branchId}/complete
Authorization: Bearer {token}
{
  "branchName": "Downtown Branch",
  "address": "123 Main St",
  "phoneNumber": "+1234567890",
  "operatingHours": "Mon-Fri: 9AM-9PM",
  "initialMenuCategories": ["Appetizers", "Mains", "Desserts"]
}
```

## Migration Notes

- Existing branches will have `setupStatus` as `ACCOUNT_CREATED`
- Backward compatibility maintained for existing APIs
- New setup system is optional - branches can use old or new method

## Future Enhancements

1. Setup progress percentage calculation
2. Setup checklist UI components
3. Email notifications for setup milestones
4. Setup templates for quick configuration
5. Bulk setup for multiple branches
