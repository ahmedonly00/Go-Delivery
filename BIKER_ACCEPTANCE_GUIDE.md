# Biker Delivery Acceptance Guide

## Overview
This guide covers the biker delivery acceptance feature, allowing bikers to confirm availability and accept or reject delivery assignments.

Base URL: `http://localhost:8085`

---

## Features

✅ **View Available Orders** - Bikers can see orders waiting for acceptance  
✅ **Accept Delivery** - Bikers confirm they will deliver the order  
✅ **Reject Delivery** - Bikers decline orders with a reason  
✅ **View Active Orders** - Bikers see their current assigned deliveries  
✅ **Customer Notifications** - Customers are notified when biker accepts  
✅ **Restaurant Notifications** - Restaurants are notified of biker acceptance/rejection  

---

## API Endpoints

### 1. Login as Biker

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "biker@mozfood.com",
  "password": "biker123"
}
```

**Response:**
```json
{
  "token": "eyJhbGci...",
  "userType": "BIKER",
  "userId": 10,
  "bikerId": 1
}
```

Save the `token` and `bikerId` for subsequent requests.

---

### 2. Get Available Orders

Get list of orders available for pickup (CONFIRMED or READY status).

```http
GET /api/bikers/{bikerId}/availableOrders
Authorization: Bearer {biker_token}
```

**Example:**
```http
GET /api/bikers/1/availableOrders
Authorization: Bearer eyJhbGci...
```

**Response:**
```json
[
  {
    "orderId": 101,
    "orderNumber": "ORD-20250930-101",
    "orderStatus": "CONFIRMED",
    "restaurantName": "Pizza Palace",
    "restaurantAddress": "456 Restaurant Ave",
    "deliveryAddress": "456 Oak Avenue, Apartment 3B",
    "customerName": "Jane Doe",
    "customerPhone": "+1234567890",
    "finalAmount": 38.47,
    "estimatedPrepTimeMinutes": 30,
    "specialInstructions": "Ring doorbell twice. No contact delivery.",
    "orderPlacedAt": "2025-09-30T11:30:00",
    "distanceToRestaurant": 2.5
  },
  {
    "orderId": 102,
    "orderNumber": "ORD-20250930-102",
    "orderStatus": "READY",
    "restaurantName": "Burger House",
    "deliveryAddress": "789 Main St",
    "finalAmount": 25.99
  }
]
```

---

### 3. Accept Delivery (🔔 TRIGGERS NOTIFICATIONS!)

Biker confirms they will deliver the order.

```http
POST /api/bikers/acceptDelivery
Authorization: Bearer {biker_token}
Content-Type: application/json

{
  "orderId": 101,
  "bikerId": 1,
  "estimatedDeliveryMinutes": 25,
  "message": "On my way to pickup"
}
```

**Request Fields:**
- `orderId` (required) - Order to accept
- `bikerId` (required) - Your biker ID
- `estimatedDeliveryMinutes` (optional) - Your estimated delivery time
- `message` (optional) - Optional message

**Response:**
```json
{
  "orderId": 101,
  "orderNumber": "ORD-20250930-101",
  "bikerId": 1,
  "bikerName": "John Biker",
  "orderStatus": "CONFIRMED",
  "restaurantName": "Pizza Palace",
  "pickupAddress": "456 Restaurant Ave",
  "deliveryAddress": "456 Oak Avenue, Apartment 3B",
  "customerName": "Jane Doe",
  "customerPhone": "+1234567890",
  "orderAmount": 38.47,
  "acceptedAt": "2025-09-30T12:05:00",
  "estimatedDeliveryMinutes": 25,
  "message": "Delivery accepted successfully. Please proceed to restaurant for pickup."
}
```

**What Happens:**
1. ✅ Order is assigned to you (biker)
2. ✅ Your availability status changes to unavailable
3. ✅ **Customer receives notification** with your details
4. ✅ **Restaurant receives notification** that you accepted
5. ✅ Other bikers no longer see this order

**Expected Console Logs:**
```
INFO: Biker 1 accepting delivery for order 101
INFO: Biker 1 successfully accepted order ORD-20250930-101
INFO: Notified customer jane@example.com that biker 1 accepted order ORD-20250930-101
INFO: Sent delivery acceptance notifications for order ORD-20250930-101
```

---

### 4. Reject Delivery

Biker declines the delivery assignment.

```http
POST /api/bikers/rejectDelivery
Authorization: Bearer {biker_token}
Content-Type: application/json

{
  "orderId": 101,
  "bikerId": 1,
  "reason": "Too far from my current location"
}
```

**Request Fields:**
- `orderId` (required) - Order to reject
- `bikerId` (required) - Your biker ID
- `reason` (required) - Reason for rejection

**Common Rejection Reasons:**
- "Too far from current location"
- "Already on another delivery"
- "Vehicle issue"
- "Traffic/weather conditions"
- "Personal emergency"

**Response:**
```
"Delivery rejected successfully. Order has been broadcast to other bikers."
```

**What Happens:**
1. ✅ If order was assigned to you, it's unassigned
2. ✅ **Restaurant receives notification** of rejection
3. ✅ **Order is broadcast to other available bikers**
4. ✅ Your availability remains unchanged

**Expected Console Logs:**
```
INFO: Biker 1 rejecting delivery for order 101 - Reason: Too far from my current location
INFO: Notified restaurant that biker 1 rejected order ORD-20250930-101
INFO: Broadcasting order ORD-20250930-101 to all available bikers
INFO: Biker 1 rejected order ORD-20250930-101. Order broadcast to other bikers.
```

---

### 5. Get Active Orders

Get list of your currently assigned orders (in progress deliveries).

```http
GET /api/bikers/{bikerId}/activeOrders
Authorization: Bearer {biker_token}
```

**Example:**
```http
GET /api/bikers/1/activeOrders
Authorization: Bearer eyJhbGci...
```

**Response:**
```json
[
  {
    "orderId": 101,
    "orderNumber": "ORD-20250930-101",
    "orderStatus": "PICKED_UP",
    "restaurantName": "Pizza Palace",
    "deliveryAddress": "456 Oak Avenue, Apartment 3B",
    "customerName": "Jane Doe",
    "customerPhone": "+1234567890",
    "finalAmount": 38.47,
    "pickedUpAt": "2025-09-30T12:15:00"
  }
]
```

---

## Complete Testing Workflow

### Scenario 1: Biker Accepts Broadcast Order

**Step 1:** Restaurant accepts order → Bikers notified
```http
POST /api/cashier/acceptOrder/101?estimatedPrepTime=30
```

**Step 2:** Biker logs in
```http
POST /api/auth/login
{"email": "biker@mozfood.com", "password": "biker123"}
```

**Step 3:** Biker views available orders
```http
GET /api/bikers/1/availableOrders
```

**Step 4:** Biker accepts delivery
```http
POST /api/bikers/acceptDelivery
{
  "orderId": 101,
  "bikerId": 1,
  "estimatedDeliveryMinutes": 25
}
```

**Step 5:** Check console for notifications
```
INFO: Notified customer that biker accepted
INFO: Notified restaurant that biker assigned
```

**Step 6:** Biker proceeds to restaurant for pickup

---

### Scenario 2: Biker Rejects Order

**Step 1-3:** Same as Scenario 1

**Step 4:** Biker rejects delivery
```http
POST /api/bikers/rejectDelivery
{
  "orderId": 101,
  "bikerId": 1,
  "reason": "Too far from current location"
}
```

**Step 5:** Order is broadcast to other bikers
```
INFO: Broadcasting order to other available bikers
```

**Step 6:** Another biker can now accept

---

## Customer Notification (When Biker Accepts)

**Email Subject:** "Your Delivery is On The Way!"

**Email Content:**
```
Good news! Your order #ORD-20250930-101 has been accepted by John Biker.

Delivery Person: John Biker
Rating: 4.8
Phone: +1234567890
Estimated Delivery: 25 minutes

You can track your order in real-time through the app.
```

---

## Restaurant Notification (When Biker Accepts)

**Email Subject:** "Biker Assigned - Order #ORD-20250930-101"

**Email Content:**
```
Biker John Biker has accepted delivery for order #ORD-20250930-101.

Biker Details:
Name: John Biker
Phone: +1234567890
Vehicle: MOTORCYCLE - ABC-123

Order should be ready for pickup soon.
```

---

## Restaurant Notification (When Biker Rejects)

**Email Subject:** "Delivery Declined - Order #ORD-20250930-101"

**Email Content:**
```
Biker John Biker has declined delivery for order #ORD-20250930-101.
Reason: Too far from current location

The order has been broadcast to other available bikers.
```

---

## Validation Rules

### Accept Delivery Validations:
- ✅ Biker must exist and be active
- ✅ Order must exist
- ✅ Order status must be CONFIRMED or READY
- ✅ Order cannot be assigned to another biker
- ✅ Biker must have valid authentication token

### Reject Delivery Validations:
- ✅ Biker must exist
- ✅ Order must exist
- ✅ Rejection reason is required

---

## Error Responses

### Biker Not Found
```json
{
  "error": "Biker not found with id: 1",
  "status": 404
}
```

### Order Not Available
```json
{
  "error": "Order is not available for delivery acceptance. Current status: DELIVERED",
  "status": 400
}
```

### Already Assigned
```json
{
  "error": "Order is already assigned to another biker",
  "status": 400
}
```

### Inactive Biker
```json
{
  "error": "Biker account is not active",
  "status": 400
}
```

---

## Postman Collection

### Environment Variables
```json
{
  "baseUrl": "http://localhost:8085",
  "bikerToken": "",
  "bikerId": "1",
  "orderId": ""
}
```

### Test Scripts

**For Login (Tests tab):**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("bikerToken", jsonData.token);
    pm.environment.set("bikerId", jsonData.bikerId);
}
```

**For Accept Delivery (Tests tab):**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    console.log("✅ Delivery accepted for order: " + jsonData.orderNumber);
    console.log("Customer: " + jsonData.customerName);
    console.log("Pickup: " + jsonData.pickupAddress);
    console.log("Delivery: " + jsonData.deliveryAddress);
}
```

---

## Testing Tips

### 1. Test with Multiple Bikers

Create multiple biker accounts and test first-come-first-served:
- Biker 1 accepts → Order assigned to Biker 1
- Biker 2 tries to accept same order → Gets error "already assigned"

### 2. Test Rejection Flow

- Biker 1 rejects → Order broadcast again
- Biker 2 accepts → Successfully assigned

### 3. Check Biker Availability

After acceptance:
```sql
SELECT is_available FROM bikers WHERE biker_id = 1;
-- Should return false
```

After delivery completion:
```sql
-- Biker should be available again
```

---

## State Diagram

```
Order Created (PLACED)
    ↓
Restaurant Accepts (CONFIRMED) → 🔔 Broadcast to all bikers
    ↓
Biker 1 Accepts → Assigned to Biker 1 → 🔔 Customer notified
    ↓
Ready for Pickup (READY)
    ↓
Biker Picks Up (PICKED_UP)
    ↓
Biker Delivers (DELIVERED)


Alternative Flow:
    ↓
Biker 1 Rejects → 🔔 Restaurant notified → Broadcast to others
    ↓
Biker 2 Accepts → Assigned to Biker 2
```

---

## Related Documentation

- **PICKUP_CONFIRMATION_GUIDE.md** - Pickup confirmation at restaurant
- **COMPLETE_TESTING_GUIDE.md** - Full system testing
- **BIKER_NOTIFICATION_SYSTEM.md** - Notification architecture
- **TESTING_QUICK_REFERENCE.md** - Quick reference card

---

## Quick cURL Commands

### Accept Delivery
```bash
curl -X POST http://localhost:8085/api/bikers/acceptDelivery \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 101,
    "bikerId": 1,
    "estimatedDeliveryMinutes": 25
  }'
```

### Reject Delivery
```bash
curl -X POST http://localhost:8085/api/bikers/rejectDelivery \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 101,
    "bikerId": 1,
    "reason": "Too far from current location"
  }'
```

### Get Available Orders
```bash
curl -X GET http://localhost:8085/api/bikers/1/availableOrders \
  -H "Authorization: Bearer {token}"
```

---

**Last Updated:** 2025-09-30  
**Version:** 1.0
