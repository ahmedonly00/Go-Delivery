# Branch Manager Setup Flow

## Overview
After a branch is created by the restaurant admin, the branch manager receives login credentials via email and must complete their branch setup similar to how restaurants complete their own setup.

## Flow Summary

### 1. Branch Creation (Restaurant Admin)
- Restaurant admin creates a branch using `/api/v1/branches/create/{restaurantId}` or `/api/v1/branch-registration/register-comprehensive/{restaurantId}`
- Branch manager account is automatically created
- Credentials are sent to the manager's email
- Branch starts with `setupStatus = ACCOUNT_CREATED`

### 2. Branch Manager Login
- Branch manager receives email with:
  - Login credentials (email + temporary password)
  - Branch information
  - Setup instructions
- Manager logs in at the provided URL
- Must change password on first login

### 3. Branch Setup Process (Branch Manager)

#### Step 0: Check Current Status
```http
GET /api/v1/branch-setup-progress/{branchId}/status
Authorization: Bearer {BRANCH_MANAGER_TOKEN}
```
Returns current setup status and branch information

#### Step 1: Add Location Details
```http
PUT /api/v1/branch-setup-progress/{branchId}/location
{
  "address": "123 Main Street",
  "latitude": 40.7128,
  "longitude": -74.0060
}
```
- Status: `LOCATION_ADDED`

#### Step 2: Configure Delivery Settings
```http
PUT /api/v1/branch-setup-progress/{branchId}/delivery-settings
{
  "deliveryType": "SELF_DELIVERY",
  "deliveryFee": 5.99,
  "deliveryRadius": 10.0,
  "minimumOrderAmount": 20.0,
  "averagePrepTime": 30
}
```
- Status: `SETTINGS_CONFIGURED`

#### Step 3: Add Operating Hours
```http
POST /api/v1/branch-setup-progress/{branchId}/operating-hours
{
  "mondayOpen": "09:00",
  "mondayClose": "22:00",
  "tuesdayOpen": "09:00",
  "tuesdayClose": "22:00",
  // ... all days
}
```
- Status: `OPERATING_HOURS_ADDED`

#### Step 4: Add Branding
```http
PUT /api/v1/branch-setup-progress/{branchId}/branding
{
  "logoUrl": "/uploads/branches/logo.jpg",
  "description": "Our downtown location offers the best dining experience"
}
```
- Status: `BRANDING_ADDED`

#### Step 5: Start Menu Setup
```http
PUT /api/v1/branch-setup-progress/{branchId}/menu-setup/start
```
- Status: `MENU_SETUP_STARTED`
- Manager can now add menu categories and items via BranchMenuController

#### Step 6: Complete Menu Setup
```http
PUT /api/v1/branch-setup-progress/{branchId}/menu-setup/complete
```
- Status: `MENU_SETUP_COMPLETED`

#### Step 7: Complete Branch Setup
```http
PUT /api/v1/branch-setup-progress/{branchId}/complete
```
- Status: `COMPLETED`
- If branch is approved: status changes to `ACTIVE`

### 4. Get Branch Information
Branch managers can get their branch ID and details:
```http
GET /api/v1/branch-users/my-branch
Authorization: Bearer {BRANCH_MANAGER_TOKEN}
```

## Security & Permissions

### Authentication
- All endpoints require `BRANCH_MANAGER` role
- JWT token required in Authorization header
- Managers can only access their own branch

### Authorization Checks
- Each endpoint verifies the authenticated user belongs to the branch
- Attempting to access another branch results in `ValidationException`

## Setup Status Flow

```
ACCOUNT_CREATED (initial)
    ↓ (location added)
LOCATION_ADDED
    ↓ (settings configured)
SETTINGS_CONFIGURED
    ↓ (hours added)
OPERATING_HOURS_ADDED
    ↓ (branding added)
BRANDING_ADDED
    ↓ (menu started)
MENU_SETUP_STARTED
    ↓ (menu completed)
MENU_SETUP_COMPLETED
    ↓ (setup complete)
COMPLETED
    ↓ (if approved)
ACTIVE
    ↕ (can be)
REJECTED/SUSPENDED
```

## Email Templates

### Branch Manager Welcome Email
- Sent immediately after account creation
- Contains:
  - Login credentials
  - Branch and restaurant information
  - Setup instructions
  - Security notice about password change
  - Step-by-step guide

## Error Handling

### Common Errors
- `401 Unauthorized`: Invalid or missing JWT token
- `403 Forbidden`: User doesn't have BRANCH_MANAGER role
- `404 Not Found`: Branch not found or user not found
- `400 Bad Request`: Invalid data or permission denied

### Error Response Format
```json
{
  "timestamp": "2025-01-09T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to update this branch",
  "path": "/api/v1/branch-setup-progress/123/status"
}
```

## Best Practices

1. **Sequential Setup**: Complete steps in order for proper flow
2. **Validation**: Each step validates required fields before proceeding
3. **Security**: Change temporary password immediately
4. **Data Persistence**: All changes are saved immediately
5. **Audit Trail**: All actions are logged for tracking

## Testing the Flow

1. Create a branch as restaurant admin
2. Check email for branch manager credentials
3. Login as branch manager
4. Follow setup steps sequentially
5. Verify status changes at each step
6. Complete setup and verify branch is active
