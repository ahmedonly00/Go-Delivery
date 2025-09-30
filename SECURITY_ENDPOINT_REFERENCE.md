# Security & Endpoint Access Reference

## Overview
This document outlines the security configuration and access control for all endpoints in the GoDelivery system.

---

## Authentication

### Login Endpoint
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userType": "BIKER",
  "userId": 10,
  "bikerId": 1
}
```

**Use the token in subsequent requests:**
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Endpoint Access Control

### 🌐 Public Endpoints (No Authentication Required)

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/auth/**` | ALL | Login, register, password reset |
| `/api/customers/registerCustomer` | POST | Customer registration |
| `/api/restaurant-applications/submit` | POST | Restaurant application |
| `/api/super-admin/register` | POST | Super admin registration |
| `/api/orders/*/track` | GET | Order tracking (customers) |
| `/api/bikers/tracking/*` | GET | Delivery tracking (customers) |
| `/swagger-ui/**` | GET | API documentation |
| `/api/test/email/**` | ALL | Email testing (dev only) |

---

### 🚴 Biker Endpoints (Require BIKER Role)

**Authentication Required:** `Authorization: Bearer {biker_token}`

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/bikers/{bikerId}/availableOrders` | GET | View available orders |
| `/api/bikers/{bikerId}/activeOrders` | GET | View active deliveries |
| `/api/bikers/{bikerId}/customerDetails/{orderId}` | GET | Get customer interaction details |
| `/api/bikers/{bikerId}/navigation/{orderId}` | GET | Get navigation to destination |
| `/api/bikers/acceptDelivery` | POST | Accept delivery assignment |
| `/api/bikers/rejectDelivery` | POST | Reject delivery assignment |
| `/api/bikers/confirmPickup` | POST | Confirm pickup at restaurant |
| `/api/bikers/confirmDelivery` | POST | Confirm delivery completion |
| `/api/bikers/updateLocation` | POST | Update GPS location |
| `/api/bikers/getNavigation` | POST | Get navigation details |

**Example Request:**
```http
GET /api/bikers/1/availableOrders
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### 🍔 Restaurant Admin Endpoints

**Authentication Required:** `Authorization: Bearer {admin_token}`  
**Role:** `RESTAURANT_ADMIN`

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/users/**` | ALL | User management (staff) |
| `/api/menu-item/**` | ALL | Menu item management |
| `/api/menu-category/**` | ALL | Category management |
| `/api/file-upload/**` | ALL | File uploads (menu images) |
| `/api/restaurants/**` | ALL | Restaurant management |
| `/api/orders/**` | GET | View orders (shared with others) |
| `/api/analytics/**` | GET | Restaurant analytics |

**Example Request:**
```http
POST /api/menu-item/create
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "itemName": "Margherita Pizza",
  "price": 12.99,
  "categoryId": 1
}
```

---

### 💰 Cashier Endpoints

**Authentication Required:** `Authorization: Bearer {cashier_token}`  
**Role:** `CASHIER`

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/cashier/**` | ALL | All cashier operations |
| `/api/cashier/acceptOrder/{orderId}` | POST | Accept order |
| `/api/cashier/assignToDelivery/{orderId}` | POST | Assign biker |
| `/api/cashier/confirmOrderDispatch/{orderId}` | POST | Confirm dispatch |
| `/api/cashier/markOrderReadyForPickup/{orderId}` | POST | Mark ready |

---

### 👤 Customer Endpoints

**Authentication Required:** `Authorization: Bearer {customer_token}`  
**Role:** `CUSTOMER`

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/customers/**` | ALL | Customer profile management |
| `/api/orders/createOrder` | POST | Place new order |
| `/api/orders/cancelOrder/*` | POST | Cancel order |
| `/api/orders/**` | GET | View orders (shared with others) |
| `/api/payments/process` | POST | Process payment |
| `/api/payments/customer/*` | GET | View payment history |

**Example Request:**
```http
POST /api/orders/createOrder
Authorization: Bearer {customer_token}
Content-Type: application/json

{
  "restaurantId": 1,
  "items": [...],
  "deliveryAddress": "123 Main St"
}
```

---

### 👑 Super Admin Endpoints

**Authentication Required:** `Authorization: Bearer {super_admin_token}`  
**Role:** `SUPER_ADMIN`

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/restaurant-applications/all` | GET | View all applications |
| `/api/restaurant-applications/status` | GET | Check status |
| `/api/restaurant-applications/*/review` | PUT | Review application |
| `/api/analytics/**` | GET | System-wide analytics |

---

## Role Hierarchy

```
SUPER_ADMIN (Highest)
    ↓
RESTAURANT_ADMIN
    ↓
CASHIER
    ↓
BIKER
    ↓
CUSTOMER
```

---

## Complete Role-Based Access Matrix

| Endpoint Pattern | CUSTOMER | RESTAURANT_ADMIN | CASHIER | BIKER | SUPER_ADMIN | PUBLIC |
|-----------------|----------|------------------|---------|-------|-------------|--------|
| `/api/auth/**` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/api/customers/**` | ✅ | ❌ | ❌ | ❌ | ❌ | Register only |
| `/api/orders/createOrder` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `/api/orders/cancelOrder/*` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `/api/orders/**` (view) | ✅ | ✅ | ✅ | ❌ | ❌ | Track only |
| `/api/payments/process` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `/api/users/**` | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `/api/menu-item/**` | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `/api/menu-category/**` | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `/api/restaurants/**` | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `/api/cashier/**` | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `/api/bikers/**` | ❌ | ❌ | ❌ | ✅ | ❌ | Tracking only |
| `/api/analytics/**` | ❌ | ✅ | ❌ | ❌ | ✅ | ❌ |
| `/api/restaurant-applications/**` | ❌ | ❌ | ❌ | ❌ | ✅ | Submit only |

---

## Testing Authentication

### 1. Login as Customer
```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@example.com",
    "password": "customer123"
  }'
```

### 2. Login as Restaurant Admin
```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@restaurant.com",
    "password": "admin123"
  }'
```

### 3. Login as Biker
```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "biker@mozfood.com",
    "password": "biker123"
  }'
```

**Save the token from response**

### 2. Use Token in Requests
```bash
curl -X GET http://localhost:8085/api/bikers/1/availableOrders \
  -H "Authorization: Bearer eyJhbGci..."
```

---

## Common HTTP Status Codes

| Code | Meaning | Cause |
|------|---------|-------|
| 200 | OK | Request successful |
| 401 | Unauthorized | No token or invalid token |
| 403 | Forbidden | Valid token but wrong role |
| 404 | Not Found | Resource doesn't exist |
| 400 | Bad Request | Invalid request data |

---

## Error Responses

### 401 Unauthorized
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed: JWT token is missing"
}
```

**Solution:** Include valid token in Authorization header

### 403 Forbidden
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied: Insufficient permissions"
}
```

**Solution:** Login with correct role (e.g., BIKER role for biker endpoints)

---

## Postman Setup

### 1. Create Environment Variables

```json
{
  "baseUrl": "http://localhost:8085",
  "bikerToken": "",
  "cashierToken": "",
  "customerToken": "",
  "adminToken": ""
}
```

### 2. Login Script (Tests Tab)

Add to login request Tests tab:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("bikerToken", jsonData.token);
    pm.environment.set("bikerId", jsonData.bikerId);
    console.log("Token saved:", jsonData.token);
}
```

### 3. Authorization Setup

For each authenticated request:
1. Go to **Authorization** tab
2. Select **Type:** `Bearer Token`
3. **Token:** `{{bikerToken}}`

Or add to **Headers:**
```
Authorization: Bearer {{bikerToken}}
```

---

## Security Features

### 1. JWT Token-Based Authentication
- Stateless authentication
- Token expires after configured time
- Token includes user role and ID

### 2. Role-Based Access Control (RBAC)
- Each endpoint restricted by role
- `@PreAuthorize` annotations in controllers
- Spring Security filters all requests

### 3. CORS Configuration
- Allows requests from `http://localhost:3000`
- Supports all standard HTTP methods
- Credentials allowed for authenticated requests

### 4. Session Management
- Stateless sessions (no server-side sessions)
- Each request must include valid token

---

## Development vs Production

### Development (Current)
```java
configuration.setAllowedOrigins(List.of("*"));
configuration.setAllowedOriginPatterns(List.of("http://localhost:3000"));
```

### Production (Recommended)
```java
configuration.setAllowedOrigins(List.of(
    "https://yourdomain.com",
    "https://app.yourdomain.com"
));
configuration.setAllowedOriginPatterns(List.of());
```

---

## Testing Checklist

### Biker Authentication Flow

- [ ] 1. Login as biker → Get token
- [ ] 2. Save token in environment
- [ ] 3. Get available orders → Should succeed (200)
- [ ] 4. Accept delivery → Should succeed (200)
- [ ] 5. Confirm pickup → Should succeed (200)
- [ ] 6. Update location → Should succeed (200)
- [ ] 7. Confirm delivery → Should succeed (200)

### Unauthorized Access Test

- [ ] 1. Try biker endpoint without token → 401 Unauthorized
- [ ] 2. Try biker endpoint with customer token → 403 Forbidden
- [ ] 3. Try admin endpoint with biker token → 403 Forbidden

---

## Troubleshooting

### "401 Unauthorized" Error

**Check:**
1. Token included in request?
2. Token format: `Bearer <token>`?
3. Token expired?
4. Token valid (not tampered)?

**Solution:**
```bash
# Login again to get fresh token
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"biker@mozfood.com","password":"biker123"}'
```

### "403 Forbidden" Error

**Check:**
1. Correct role for endpoint?
2. BIKER role in JWT token?
3. Endpoint path correct?

**Solution:**
- Login with correct user type
- Verify role in JWT token (decode at jwt.io)

### "CORS Error" in Browser

**Check:**
1. Origin allowed in CORS config?
2. Credentials set correctly?

**Solution:**
```java
configuration.setAllowedOriginPatterns(List.of("http://localhost:3000"));
configuration.setAllowCredentials(true);
```

---

## Quick Reference Card

### Biker Login
```bash
POST /api/auth/login
{"email":"biker@mozfood.com","password":"biker123"}
```

### Get Available Orders
```bash
GET /api/bikers/1/availableOrders
Authorization: Bearer {token}
```

### Accept Delivery
```bash
POST /api/bikers/acceptDelivery
Authorization: Bearer {token}
{"orderId":101,"bikerId":1}
```

### Confirm Pickup
```bash
POST /api/bikers/confirmPickup
Authorization: Bearer {token}
{"orderId":101,"bikerId":1,"orderVerified":true}
```

### Confirm Delivery
```bash
POST /api/bikers/confirmDelivery
Authorization: Bearer {token}
{"orderId":101,"bikerId":1,"recipientName":"Jane Doe"}
```

---

## Related Documentation

- `BIKER_FEATURES_SUMMARY.md` - Complete biker features
- `TESTING_QUICK_REFERENCE.md` - API testing guide
- `COMPLETE_TESTING_GUIDE.md` - End-to-end testing

---

**Last Updated:** 2025-09-30  
**Version:** 1.0
