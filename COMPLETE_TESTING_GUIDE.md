# Complete End-to-End Testing Guide - GoDelivery System

## Quick Start
Base URL: `http://localhost:8085`

This guide covers the complete order lifecycle: Login → Order Creation → Restaurant Accept → **Biker Notifications** → Delivery → Analytics

---

## 1️⃣ Authentication

### Login as Customer
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "customer@example.com",
  "password": "password123"
}
```

**Response:** Save the `token` for authenticated requests
```json
{
  "token": "eyJhbG...",
  "userType": "CUSTOMER",
  "userId": 1
}
```

### Login as Cashier
```json
{
  "email": "cashier@restaurant.com",
  "password": "cashier123"
}
```

### Login as Biker
```json
{
  "email": "biker@mozfood.com",
  "password": "biker123"
}
```

### Login as Restaurant Admin
```json
{
  "email": "admin@restaurant.com",
  "password": "admin123"
}
```

---

## 2️⃣ Register Customer (Optional)

```http
POST /api/customers/registerCustomer
Content-Type: application/json

{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "password": "SecurePass123!",
  "phoneNumber": "+1234567890",
  "address": "123 Main Street, Apt 4B",
  "city": "Cityville",
  "postalCode": "12345"
}
```

---

## 3️⃣ Create Order

```http
POST /api/orders/createOrder
Authorization: Bearer {customer_token}
Content-Type: application/json

{
  "customerId": 1,
  "restaurantId": 1,
  "deliveryAddress": "456 Oak Avenue, Apartment 3B, Cityville, 12345",
  "specialInstructions": "Ring doorbell twice. No contact delivery.",
  "paymentMethod": "CARD",
  "orderItems": [
    {
      "menuItemId": 1,
      "quantity": 2,
      "specialInstructions": "Extra cheese, no onions"
    },
    {
      "menuItemId": 2,
      "quantity": 1
    }
  ]
}
```

**Response:** Save `orderId`
```json
{
  "orderId": 101,
  "orderNumber": "ORD-20250930-101",
  "orderStatus": "PLACED",
  "finalAmount": 38.47
}
```

---

## 4️⃣ Cashier Accept Order (🔔 TRIGGERS BIKER NOTIFICATIONS!)

```http
POST /api/cashier/acceptOrder/101?estimatedPrepTime=30
Authorization: Bearer {cashier_token}
```

**What Happens:**
1. ✅ Order status: `PLACED` → `CONFIRMED`
2. ✅ Customer receives notification
3. 🚨 **BIKER NOTIFICATION SYSTEM TRIGGERED!**
   - Sends Push Notification to available bikers
   - Sends SMS to bikers
   - Sends Email to bikers
4. ✅ Check console logs for notifications

**Expected Console Logs:**
```
INFO: Accepting order ID: 101 with estimated prep time: 30 minutes
INFO: Broadcasting order ORD-20250930-101 to all available bikers
INFO: Found 3 available bikers
INFO: Sending push notification to user: 1
INFO: Title: New Order Available!
INFO: Sending SMS to: +1234567890
INFO: Message: New order #ORD-20250930-101 ready at Pizza Palace
INFO: Sending email to: biker@mozfood.com
INFO: Sent new order notification to biker 1 for order ORD-20250930-101
```

**Response:**
```json
{
  "orderId": 101,
  "orderStatus": "CONFIRMED",
  "estimatedPrepTimeMinutes": 30
}
```

---

## 5️⃣ View Pending Orders (Cashier)

```http
GET /api/cashier/getPendingOrders?page=0&size=20
Authorization: Bearer {cashier_token}
```

---

## 6️⃣ Update Order Status (Kitchen Workflow)

### Mark as PREPARING
```http
PUT /api/cashier/updateOrderStatus
Authorization: Bearer {cashier_token}
Content-Type: application/json

{
  "orderId": 101,
  "status": "PREPARING"
}
```

### Mark Ready for Pickup
```http
POST /api/cashier/markOrderReadyForPickup/101
Authorization: Bearer {cashier_token}
```

---

## 7️⃣ Assign Biker & Dispatch

### Assign Biker to Order (Also sends notification!)
```http
POST /api/cashier/assignToDelivery/101?bikerId=1
Authorization: Bearer {cashier_token}
```

### Confirm Dispatch (Order Picked Up)
```http
POST /api/cashier/confirmOrderDispatch/101
Authorization: Bearer {cashier_token}
```

**Response:**
```json
{
  "orderId": 101,
  "orderStatus": "PICKED_UP",
  "bikerName": "John Biker"
  "deliveryPersonName": "John Biker",
  "deliveryPersonContact": "+1234567890",
  "deliveryPersonRating": 4.8,
  "distanceRemaining": 2.5,
  "estimatedMinutesRemaining": 15,
  "statusHistory": [...]
}
```

---

## 9️⃣ Mark Order Delivered

```http
PUT /api/orders/updateOrderStatus/101
Authorization: Bearer {biker_token}
Content-Type: application/json

{
  "status": "DELIVERED"
}
```

---

## 🔟 Analytics (Restaurant Admin)

### Get Order History
```http
GET /api/analytics/getOrdersHistory?restaurantId=1&startDate=2025-09-01&endDate=2025-09-30
Authorization: Bearer {admin_token}
```

### Generate Sales Report
```http
GET /api/analytics/getSalesReport?restaurantId=1&startDate=2025-09-01&endDate=2025-09-30&period=DAILY
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "restaurantId": 1,
  "period": "DAILY",
  "totalOrders": 150,
  "totalRevenue": 5247.50,
  "averageOrderValue": 34.98,
  "dailyBreakdown": [...]
}
```

### Analyze Customer Trends
```http
GET /api/analytics/getCustomerTrends?restaurantId=1&startDate=2025-09-01&endDate=2025-09-30
Authorization: Bearer {admin_token}
```

---

## 📋 Complete Testing Workflow

### Full Order Lifecycle

1. **Customer Login** (Section 1)
2. **Create Order** (Section 3) → Status: `PLACED`
3. **Cashier Login** (Section 1)
4. **Accept Order** (Section 4) → Status: `CONFIRMED` → **🔔 Bikers Notified!**
5. **Update to PREPARING** (Section 6)
6. **Mark Ready** (Section 6) → Status: `READY`
7. **Assign Biker** (Section 7) → **🔔 Specific Biker Notified!**
8. **Confirm Dispatch** (Section 7) → Status: `PICKED_UP`
9. **Customer Tracks Order** (Section 8)
10. **Mark Delivered** (Section 9) → Status: `DELIVERED`
11. **View Analytics** (Section 10)

---

## 🔔 Biker Notification Details

### When Notifications Are Sent

| Action | Notification Trigger | Recipients |
|--------|---------------------|------------|
| Cashier accepts order | Order status → CONFIRMED | All available bikers OR assigned biker |
| Assign biker to order | Biker assignment | Specific assigned biker |

### Notification Channels

**1. Push Notification**
```json
{
  "title": "New Order Available!",
  "message": "New order #ORD-20250930-101 is ready for pickup!",
  "data": {
    "orderNumber": "ORD-20250930-101",
    "restaurantName": "Pizza Palace",
    "pickupAddress": "456 Restaurant Ave",
    "deliveryAddress": "456 Oak Avenue, Apt 3B",
    "type": "NEW_ORDER"
  }
}
```

**2. SMS**
```
New order #ORD-20250930-101 ready at Pizza Palace. Check app for details.
```

**3. Email**
- Subject: "New Delivery Order #ORD-20250930-101"
- Full order details, addresses, instructions

### Biker Availability Criteria

Bikers receive notifications if:
- `isOnline = true`
- `isAvailable = true`
- `isActive = true`

---

## 🛠️ Postman Setup

### Environment Variables
```json
{
  "baseUrl": "http://localhost:8085",
  "customerToken": "",
  "cashierToken": "",
  "bikerToken": "",
  "adminToken": "",
  "orderId": "",
  "customerId": "1",
  "restaurantId": "1",
  "bikerId": "1"
}
```

### Auto-Save Token Script

Add to Login requests "Tests" tab:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("customerToken", jsonData.token);
}
```

### Auto-Save Order ID

Add to Create Order "Tests" tab:
```javascript
var jsonData = pm.response.json();
pm.environment.set("orderId", jsonData.orderId);
```

---

## 📊 Order Status Flow

```
PLACED → CONFIRMED → PREPARING → READY → PICKED_UP → DELIVERED
         ↑                                ↑
         Bikers Notified             Biker Notified
```

---

## 🗄️ Test Data SQL

### Insert Test Biker
```sql
INSERT INTO bikers (
    full_names, email, password, roles, phone_number,
    national_id, license_number, vehicle_type, vehicle_plate,
    vehicle_model, profile_image, rating, total_deliveries,
    successful_deliveries, current_latitude, current_longitude,
    is_available, is_online, is_active, joined_at, last_active
) VALUES (
    'John Biker', 'biker@mozfood.com', '$2a$10$hashedPassword', 'BIKER', '+1234567890',
    'ID123', 'LIC789', 'MOTORCYCLE', 'ABC-123',
    'Honda', 'profile.jpg', 4.8, 150, 145,
    40.7128, -74.0060, true, true, true,
    CURRENT_DATE, CURRENT_DATE
);
```

### Insert Test Customer
```sql
INSERT INTO customers (full_names, email, password, roles, phone_number, address, joined_at, is_active)
VALUES ('Jane Doe', 'customer@example.com', '$2a$10$hashedPassword', 'CUSTOMER', '+1234567890', '123 Main St', CURRENT_DATE, true);
```

---

## ✅ Verification Checklist

After running through the workflow:

- [ ] Customer logged in successfully
- [ ] Order created with status `PLACED`
- [ ] Cashier can view pending orders
- [ ] Order status changes to `CONFIRMED` after acceptance
- [ ] **Console shows "Broadcasting order to all available bikers"**
- [ ] **Console shows "Sending push notification"**
- [ ] **Console shows "Sending SMS to: {phone}"**
- [ ] **Console shows "Sending email to: {email}"**
- [ ] **Console shows "Sent new order notification to biker"**
- [ ] Biker can be assigned
- [ ] Order progresses through all statuses
- [ ] Customer can track order
- [ ] Order marked as delivered
- [ ] Analytics display correct data

---

## 🐛 Troubleshooting

### No Biker Notifications
**Problem:** Logs don't show biker notifications  
**Solution:** 
- Check biker records exist with `is_online=true`, `is_available=true`, `is_active=true`
- Verify order status changed to `CONFIRMED`
- Check application logs for errors

### 403 Forbidden
**Problem:** Access denied on endpoints  
**Solution:**
- Ensure you're using the correct role's token
- Check token hasn't expired
- Verify Authorization header format: `Bearer {token}`

### Order Not Found
**Problem:** 404 error when accessing order  
**Solution:**
- Verify orderId exists
- Use `GET /api/orders/getOrderById/{orderId}` to check

### No Available Bikers
**Problem:** Log says "No available bikers found"  
**Solution:**
- Insert test biker data (see Test Data SQL section)
- Set biker status: `UPDATE bikers SET is_online=true, is_available=true WHERE biker_id=1`

---

## 📞 Support

For system architecture details, see: `BIKER_NOTIFICATION_SYSTEM.md`

---

## 🎯 Quick Test Commands (cURL)

### Create Order
```bash
curl -X POST http://localhost:8085/api/orders/createOrder \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "restaurantId": 1,
    "deliveryAddress": "456 Oak Ave",
    "paymentMethod": "CARD",
    "orderItems": [{"menuItemId": 1, "quantity": 2}]
  }'
```

### Accept Order (Trigger Notifications)
```bash
curl -X POST "http://localhost:8085/api/cashier/acceptOrder/101?estimatedPrepTime=30" \
  -H "Authorization: Bearer {cashier_token}"
```

### Track Order
```bash
curl -X GET "http://localhost:8085/api/orders/101/track" \
  -H "Authorization: Bearer {customer_token}"
```

---

**Last Updated:** 2025-09-30  
**Version:** 1.0
