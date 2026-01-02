# Branch Setup Workflow Documentation

## Overview
After a restaurant admin creates a branch, the branch manager can log in to complete the branch setup, including location details, operating information, and initial menu configuration.

## Workflow Steps

### 1. Restaurant Admin Creates Branch
- **Endpoint**: `POST /api/v1/branches/addBranch/{restaurantId}`
- Creates a basic branch record with minimal information
- Branch status: `PENDING` approval
- Branch manager account is created during this process

### 2. Branch Manager Login
- **Endpoint**: `POST /api/auth/login`
- Branch manager uses credentials provided during branch creation
- JWT token includes branch information

### 3. Branch Setup Completion
- **Endpoint**: `POST /api/v1/branch-setup/{branchId}/complete`
- **Method**: POST with multipart/form-data
- **Request Parts**:
  - `setupData`: JSON with branch details
  - `menuImages`: Optional array of images for menu categories

#### Setup Data Structure:
```json
{
  "branchName": "Downtown Branch",
  "address": "123 Main Street, Downtown",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "phoneNumber": "+1234567890",
  "operatingHours": "Mon-Sun: 9:00 AM - 10:00 PM",
  "description": "Our downtown location serving fresh meals",
  "contactEmail": "downtown@restaurant.com",
  "deliveryRadius": "5",
  "specialInstructions": "Located near City Hall",
  "initialMenuCategories": ["Appetizers", "Main Courses", "Desserts", "Beverages"]
}
```

### 4. Update Branch Location (Optional)
- **Endpoint**: `PUT /api/v1/branch-setup/{branchId}/location`
- **Query Parameters**:
  - `latitude`: Float
  - `longitude`: Float
  - `address`: String

### 5. Create Menu Categories in Bulk (Optional)
- **Endpoint**: `POST /api/v1/branch-setup/{branchId}/menu/categories/bulk`
- **Request Parts**:
  - `categoryNames`: List of category names
  - `images`: Optional array of category images

## API Endpoints Summary

### Branch Setup
```
POST /api/v1/branch-setup/{branchId}/complete
GET /api/v1/branch-setup/{branchId}/status
PUT /api/v1/branch-setup/{branchId}/location
POST /api/v1/branch-setup/{branchId}/menu/categories/bulk
```

### Branch Menu Management (After Setup)
```
POST /api/v1/branches/{branchId}/menu/categories
POST /api/v1/branches/{branchId}/menu/categories/{categoryId}/items
GET /api/v1/branches/{branchId}/menu/categories
PUT /api/v1/branches/{branchId}/menu/categories/{categoryId}
DELETE /api/v1/branches/{branchId}/menu/categories/{categoryId}
```

## Security and Permissions

### Access Control
- Only users with `BRANCH_MANAGER` role can complete branch setup
- Branch managers can only setup branches they are assigned to
- Setup can only be completed once
- Branch must be approved before it becomes active

### Setup Verification
- System verifies the authenticated user belongs to the branch
- Checks if setup is already complete
- Validates all required fields

## Database Changes

### BranchUsers Table
- `setup_complete` field tracks if setup is completed
- `updated_at` field tracks last modification

### Branches Table
- Location fields (latitude, longitude, address)
- Operating details (hours, description, contact)
- Approval status tracking

## Frontend Implementation Guide

### Step 1: Check Setup Status
```javascript
GET /api/v1/branch-setup/{branchId}/status
```
- Returns current branch details
- Check if setup is complete via user profile

### Step 2: Setup Form
- Multi-step form for branch details
- Location picker for coordinates
- Image upload for menu categories
- Progress indicator

### Step 3: Submit Setup
```javascript
POST /api/v1/branch-setup/{branchId}/complete
Content-Type: multipart/form-data
```
- Submit all branch details
- Upload menu category images
- Handle success/error states

## Error Handling

### Common Errors
- `401 Unauthorized`: User not logged in or wrong permissions
- `403 Forbidden`: User doesn't belong to this branch
- `409 Conflict`: Setup already completed
- `400 Bad Request`: Invalid data or missing fields

### Error Response Example:
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Branch setup is already complete"
}
```

## Testing Scenarios

### Test Case 1: Successful Setup
1. Create branch as restaurant admin
2. Login as branch manager
3. Complete setup with valid data
4. Verify branch is active

### Test Case 2: Setup Validation
1. Try to setup with invalid data
2. Verify validation errors
3. Try to setup twice
4. Verify conflict error

### Test Case 3: Permission Tests
1. Try to setup as restaurant admin
2. Try to setup as different branch manager
3. Verify access denied errors

## Post-Setup Features

After setup completion, branch managers can:
- Manage menu items and categories
- Process orders
- View analytics
- Manage staff
- Update branch settings

## Notifications

Consider implementing:
- Email notification to restaurant admin when setup is complete
- In-app notification for successful setup
- Reminder notifications for incomplete setup
