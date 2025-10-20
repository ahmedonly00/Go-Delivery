# Restaurant User Management - RESTAURANT_ADMIN Capabilities

## Overview
RESTAURANT_ADMIN users can now create and manage other users (CASHIER and BIKER) for their restaurant through the existing user management API.

## Implementation Details

### Updated Files
1. **UsersService.java** - Added RESTAURANT_ADMIN permissions and role validation
2. **RestaurantUserRequest.java** - Made role and permissions optional with defaults

### Permissions

#### RESTAURANT_ADMIN Can:
- ✅ Create CASHIER users
- ✅ Create BIKER users
- ✅ Update users from their restaurant
- ✅ Delete users from their restaurant
- ✅ Activate/Deactivate users from their restaurant
- ✅ Update roles (CASHIER and BIKER only)
- ✅ View all users from their restaurant

#### RESTAURANT_ADMIN Cannot:
- ❌ Create RESTAURANT_ADMIN users
- ❌ Create SUPER_ADMIN users
- ❌ Assign RESTAURANT_ADMIN or SUPER_ADMIN roles
- ❌ Manage users from other restaurants
- ❌ Create users for other restaurants

### API Endpoints

All endpoints require authentication with RESTAURANT_ADMIN role.

#### 1. Create User
```http
POST /api/users/createUser/{restaurantId}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "phoneNumber": "1234567890",
  "role": "CASHIER",  // Optional - defaults to CASHIER
  "permissions": "READ_ORDERS,UPDATE_ORDERS,PROCESS_PAYMENTS",  // Optional - auto-set based on role
  "restaurantId": 1,
  "isActive": true
}
```

**Response:**
```json
{
  "userId": 123,
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "1234567890",
  "role": "CASHIER",
  "permissions": "READ_ORDERS,UPDATE_ORDERS,PROCESS_PAYMENTS",
  "isActive": true,
  "restaurantId": 1,
  "restaurantName": "My Restaurant",
  "createdAt": "2025-10-20T11:30:00",
  "updatedAt": "2025-10-20T11:30:00"
}
```

#### 2. Update User
```http
PUT /api/users/updateUser/{userId}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "fullName": "John Updated",
  "phoneNumber": "9876543210",
  "password": "newPassword123",  // Optional
  "role": "BIKER",  // Optional
  "permissions": "READ_DELIVERIES,UPDATE_DELIVERIES"  // Optional
}
```

#### 3. Delete User
```http
DELETE /api/users/deleteUser/{userId}
Authorization: Bearer {jwt_token}
```

#### 4. Get User by ID
```http
GET /api/users/getUserById/{userId}
Authorization: Bearer {jwt_token}
```

#### 5. Get All Users by Restaurant
```http
GET /api/users/getAllUsersByRestaurant/{restaurantId}
Authorization: Bearer {jwt_token}
```

#### 6. Get Active Users by Restaurant
```http
GET /api/users/getActiveUsersByRestaurant/{restaurantId}
Authorization: Bearer {jwt_token}
```

#### 7. Update User Role
```http
PUT /api/users/updateUserRole/{userId}/role/{role}
Authorization: Bearer {jwt_token}

// Example: /api/users/updateUserRole/123/role/BIKER
```

#### 8. Deactivate User
```http
POST /api/users/deactivateUser/{userId}
Authorization: Bearer {jwt_token}
```

#### 9. Activate User
```http
POST /api/users/activateUser/{userId}
Authorization: Bearer {jwt_token}
```

### Default Permissions by Role

The system automatically assigns default permissions based on the role:

| Role | Default Permissions |
|------|-------------------|
| CASHIER | READ_ORDERS, UPDATE_ORDERS, PROCESS_PAYMENTS |
| BIKER | READ_DELIVERIES, UPDATE_DELIVERIES |
| RESTAURANT_ADMIN | FULL_ACCESS |
| SUPER_ADMIN | SUPER_ACCESS |

### Validation Rules

1. **Email Uniqueness**: Email must be unique across all users
2. **Password**: Minimum 6 characters required for new users
3. **Phone Number**: Must be 10-15 digits
4. **Role Restrictions**: RESTAURANT_ADMIN can only create/assign CASHIER and BIKER roles
5. **Restaurant Ownership**: RESTAURANT_ADMIN can only manage users from their own restaurant

### Error Responses

#### Insufficient Permissions
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Restaurant admins can only create CASHIER and BIKER users"
}
```

#### Email Already Exists
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "User with email john.doe@example.com already exists"
}
```

#### Cross-Restaurant Access
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Cannot modify users from different restaurant"
}
```

#### User Not Found
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 123"
}
```

### Security Features

1. **Role-Based Access Control**: Only RESTAURANT_ADMIN, ADMIN, and SUPER_ADMIN can manage users
2. **Restaurant Isolation**: RESTAURANT_ADMIN can only manage users from their own restaurant
3. **Role Restrictions**: Cannot create or assign privileged roles (RESTAURANT_ADMIN, SUPER_ADMIN)
4. **Password Encryption**: All passwords are encrypted using BCrypt
5. **JWT Authentication**: All endpoints require valid JWT token

### Example Usage Flow

1. **RESTAURANT_ADMIN logs in** and receives JWT token
2. **Creates a Cashier**:
   ```bash
   curl -X POST http://localhost:8085/api/users/createUser/1 \
     -H "Authorization: Bearer {token}" \
     -H "Content-Type: application/json" \
     -d '{
       "fullName": "Jane Smith",
       "email": "jane@restaurant.com",
       "password": "password123",
       "phoneNumber": "1234567890",
       "role": "CASHIER",
       "restaurantId": 1
     }'
   ```

3. **Views all restaurant users**:
   ```bash
   curl -X GET http://localhost:8085/api/users/getAllUsersByRestaurant/1 \
     -H "Authorization: Bearer {token}"
   ```

4. **Updates user role**:
   ```bash
   curl -X PUT http://localhost:8085/api/users/updateUserRole/123/role/BIKER \
     -H "Authorization: Bearer {token}"
   ```

5. **Deactivates user**:
   ```bash
   curl -X POST http://localhost:8085/api/users/deactivateUser/123 \
     -H "Authorization: Bearer {token}"
   ```

## Frontend Integration

### Create User Form
```javascript
const createUser = async (userData) => {
  const response = await fetch(`/api/users/createUser/${restaurantId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      fullName: userData.fullName,
      email: userData.email,
      password: userData.password,
      phoneNumber: userData.phoneNumber,
      role: userData.role || 'CASHIER',  // Default to CASHIER
      restaurantId: restaurantId
    })
  });
  return response.json();
};
```

### Role Selection
```javascript
const allowedRoles = ['CASHIER', 'BIKER'];  // Only these roles for RESTAURANT_ADMIN

<select name="role">
  <option value="CASHIER">Cashier</option>
  <option value="BIKER">Biker</option>
</select>
```

## Notes

- All user operations are audited with `createdAt` and `updatedAt` timestamps
- Users can be soft-deleted using the deactivate endpoint instead of hard deletion
- The system prevents creating duplicate emails
- RESTAURANT_ADMIN must have verified email (from email verification feature) to access these endpoints
