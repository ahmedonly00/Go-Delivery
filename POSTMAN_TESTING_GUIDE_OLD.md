# Complete End-to-End API Testing Guide - GoDelivery System

## Overview
Complete guide for testing the entire GoDelivery system: authentication → order creation → biker notifications → delivery → analytics.

## Prerequisites
- ✅ Spring Boot app running on port **8085**
- ✅ Postman installed
- ✅ Database configured

## Base URL: `http://localhost:8085`

## Table of Contents
1. [Authentication](#1-authentication)
2. [Customer Registration](#2-customer-registration)
3. [Create Order](#3-create-order)
4. [Cashier Accept Order (Triggers Notifications)](#4-cashier-accept-order)
5. [Biker Notifications](#5-biker-notifications)
6. [Order Tracking & Delivery](#6-order-tracking--delivery)
7. [Analytics](#7-analytics)
8. [Complete Workflow](#8-complete-workflow)

---

## Testing Flow

### Step 1: Create a New Order

**Endpoint:** `POST /api/orders/createOrder`

**Request Body:**
```json
{
  "customerId": 1,
  "restaurantId": 1,
  "deliveryAddress": "456 Oak Avenue, Apartment 3B, Cityville",
  "specialInstructions": "Please ring the doorbell twice",
  "paymentMethod": "CARD",
  "orderItems": [
    {
      "menuItemId": 1,
      "quantity": 2,
      "specialInstructions": "Extra cheese, no onions"
    },
    {
      "menuItemId": 2,
      "quantity": 1,
      "specialInstructions": "Well done"
    }
  ]
}
```

**Expected Response:**
```json
{
  "orderId": 123,
  "orderNumber": "ORD-20250930-001",
  "orderStatus": "PLACED",
  "customerId": 1,
  "restaurantId": 1,
  "deliveryAddress": "456 Oak Avenue, Apartment 3B, Cityville",
  "subTotal": 25.50,
  "finalAmount": 25.50,
  "orderPlacedAt": "2025-09-30"
}
```

**Important:** Save the `orderId` from the response for the next step.

---

### Step 2: Accept the Order (This Triggers Biker Notifications!)

**Endpoint:** `POST /api/cashier/acceptOrder/{orderId}`

**URL Parameters:**
- `orderId` - The order ID from Step 1 (e.g., 123)
- `estimatedPrepTime` - Estimated preparation time in minutes (e.g., 30)

**Full URL Example:**
```
POST http://localhost:8085/api/cashier/acceptOrder/123?estimatedPrepTime=30
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your-jwt-token>  // If authentication is enabled
```

**Expected Response:**
```json
{
  "orderId": 123,
  "orderNumber": "ORD-20250930-001",
  "orderStatus": "CONFIRMED",
  "customerId": 1,
  "restaurantId": 1,
  "deliveryAddress": "456 Oak Avenue, Apartment 3B, Cityville",
  "estimatedPrepTimeMinutes": 30,
  "orderConfirmedAt": "2025-09-30"
}
```

**What Happens:**
1. Order status changes from `PLACED` to `CONFIRMED`
2. `OrderStatusUpdateService` detects the status change
3. Biker notification system is triggered:
   - If biker is assigned: Specific biker is notified
   - If no biker assigned: All available bikers are notified

---

### Step 3: Check Application Logs

Open your application console/logs and look for these messages:

**Expected Log Outputs:**

```
INFO: Accepting order ID: 123 with estimated prep time: 30 minutes
INFO: Sent status update notification for order 123: PLACED -> CONFIRMED
INFO: Broadcasting order ORD-20250930-001 to all available bikers
INFO: Found X available bikers
INFO: Sending push notification to user: {bikerId}
INFO: Title: New Order Available!
INFO: Sending SMS to: {bikerPhone}
INFO: Message: New order #ORD-20250930-001 ready at {restaurantName}
INFO: Sending email to: {bikerEmail}
INFO: Sent new order notification to biker {bikerId} for order ORD-20250930-001
```

---

## Optional: Test with Assigned Biker

### Step 4: Assign a Biker to the Order

**Endpoint:** `POST /api/cashier/assignToDelivery/{orderId}`

**URL Parameters:**
- `orderId` - The order ID
- `bikerId` - The biker ID to assign

**Full URL Example:**
```
POST http://localhost:8085/api/cashier/assignToDelivery/123?bikerId=1
```

**Expected Response:**
```json
{
  "orderId": 123,
  "orderNumber": "ORD-20250930-001",
  "orderStatus": "CONFIRMED",
  "bikerId": 1,
  "bikerName": "John Doe"
}
```

**Note:** This endpoint also triggers the notification to the assigned biker.

---

## Authentication Setup (If Required)

If your endpoints require authentication, you need to login first:

### Login as Cashier

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "cashier@restaurant.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userType": "CASHIER"
}
```

**Then add to Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Complete Postman Collection

### Import This Collection

Create a new Postman collection and import these requests:

#### Collection Variables
- `baseUrl`: `http://localhost:8085`
- `orderId`: (will be set from response)
- `token`: (will be set from login response)

#### Request 1: Create Order
```
POST {{baseUrl}}/api/orders/createOrder
Body: (JSON from Step 1)
```

#### Request 2: Accept Order & Trigger Notifications
```
POST {{baseUrl}}/api/cashier/acceptOrder/{{orderId}}?estimatedPrepTime=30
Headers: Authorization: Bearer {{token}}
```

#### Request 3: Get Order Details
```
GET {{baseUrl}}/api/cashier/getOrderDetails/{{orderId}}
Headers: Authorization: Bearer {{token}}
```

---

## Testing Scenarios

### Scenario 1: Broadcast to All Available Bikers

**Setup:**
1. Create an order (Step 1)
2. **Do NOT assign a biker**
3. Accept the order (Step 2)

**Expected Result:**
- All bikers with `isOnline=true`, `isAvailable=true`, `isActive=true` receive notifications
- Check logs for: "Broadcasting order {orderNumber} to all available bikers"

---

### Scenario 2: Notify Specific Assigned Biker

**Setup:**
1. Create an order (Step 1)
2. Assign a biker (Step 4)
3. Accept the order (Step 2) OR the assignment itself triggers notification

**Expected Result:**
- Only the assigned biker receives notification
- Check logs for: "Notified assigned biker {bikerId} about order {orderNumber}"

---

### Scenario 3: No Available Bikers

**Setup:**
1. Set all bikers to `isAvailable=false` or `isOnline=false` in database
2. Create and accept an order

**Expected Result:**
- Log message: "No available bikers to notify for order {orderNumber}"
- Order is still accepted successfully

---

## Verification Checklist

After triggering the notification, verify:

- [ ] Order status changed to `CONFIRMED`
- [ ] Console shows "Sending push notification" log
- [ ] Console shows "Sending SMS" log with phone number
- [ ] Console shows "Sending email" log with email address
- [ ] Notification includes correct order number
- [ ] Notification includes restaurant name
- [ ] Notification includes pickup and delivery addresses
- [ ] No errors in application logs

---

## Common Issues & Solutions

### Issue 1: "Order not found"
**Solution:** Verify the orderId exists using `GET /api/orders/getOrderById/{orderId}`

### Issue 2: "Only PLACED orders can be accepted"
**Solution:** Check order status. If already CONFIRMED, create a new order.

### Issue 3: "Access Denied" or 403 Error
**Solution:** 
- Add authentication token to headers
- Ensure user has CASHIER or ADMIN role
- Or temporarily disable security for testing

### Issue 4: No logs appear
**Solution:** 
- Check application is running
- Verify logging level is set to INFO or DEBUG
- Check `application.properties` for logging configuration

### Issue 5: "No available bikers found"
**Solution:**
- Insert test biker data:
```sql
INSERT INTO bikers (full_names, email, phone_number, is_online, is_available, is_active, ...) 
VALUES ('Test Biker', 'biker@test.com', '+1234567890', true, true, true, ...);
```

---

## Sample Test Data

### Insert Test Biker (SQL)
```sql
INSERT INTO bikers (
    full_names, email, password, roles, phone_number, 
    national_id, license_number, vehicle_type, vehicle_plate, 
    vehicle_model, profile_image, rating, total_deliveries, 
    successful_deliveries, current_latitude, current_longitude, 
    is_available, is_online, is_active, joined_at, last_active
) VALUES (
    'John Biker', 'john.biker@mozfood.com', 'hashedPassword123', 'BIKER', 
    '+1234567890', 'ID123456', 'LIC789', 'MOTORCYCLE', 'ABC-123', 
    'Honda CBR', 'profile.jpg', 4.8, 150, 145, 
    40.7128, -74.0060, true, true, true, 
    CURRENT_DATE, CURRENT_DATE
);
```

---

## Postman Environment Setup

Create a Postman Environment with these variables:

```json
{
  "name": "GoDelivery Development",
  "values": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8085",
      "enabled": true
    },
    {
      "key": "port",
      "value": "8085",
      "enabled": true
    },
    {
      "key": "token",
      "value": "",
      "enabled": true
    },
    {
      "key": "orderId",
      "value": "",
      "enabled": true
    },
    {
      "key": "bikerId",
      "value": "1",
      "enabled": true
    }
  ]
}
```

---

## Advanced Testing

### Test with cURL (Alternative to Postman)

**Create Order:**
```bash
curl -X POST http://localhost:8085/api/orders/createOrder \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "restaurantId": 1,
    "deliveryAddress": "456 Oak Avenue",
    "paymentMethod": "CARD",
    "orderItems": [{"menuItemId": 1, "quantity": 2}]
  }'
```

**Accept Order:**
```bash
curl -X POST "http://localhost:8085/api/cashier/acceptOrder/123?estimatedPrepTime=30" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## Monitoring Real-Time Notifications

Since the current implementation logs notifications, you can:

1. **Tail the application logs:**
```bash
# If running with Maven
mvn spring-boot:run | grep "notification"

# If running JAR
java -jar target/goDelivery.jar | grep "notification"
```

2. **Use Spring Boot Actuator** (if enabled):
```
GET http://localhost:8085/actuator/loggers/com.goDelivery.goDelivery.service
```

---

## Next Steps: Production Integration

For production, replace the mock notifications with real services:

1. **Push Notifications:** Integrate Firebase Cloud Messaging (FCM)
2. **SMS:** Integrate Twilio or AWS SNS
3. **Email:** Integrate SendGrid or AWS SES

Update the `NotificationService` implementation accordingly.

---

## Support

If you encounter issues:
1. Check application logs for error messages
2. Verify database has test data
3. Ensure all required entities exist (Customer, Restaurant, MenuItem, Biker)
4. Check `BIKER_NOTIFICATION_SYSTEM.md` for system architecture details
