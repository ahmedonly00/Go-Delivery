# Pickup Confirmation Guide

## Overview
This guide covers the pickup confirmation feature where bikers arrive at restaurants, verify orders, and confirm pickup in the system.

Base URL: `http://localhost:8085`

---

## Feature Description

When a biker arrives at the restaurant to pickup an order, they:
1. ✅ **Arrive at restaurant** location
2. ✅ **Verify order contents** with restaurant staff  
3. ✅ **Confirm pickup** in the system
4. ✅ **Receive navigation** to customer location
5. ✅ **Customer is notified** order is on the way

---

## API Endpoint

### Confirm Pickup

```http
POST /api/bikers/confirmPickup
Authorization: Bearer {biker_token}
Content-Type: application/json

{
  "orderId": 101,
  "bikerId": 1,
  "verificationCode": "VERIFY123",
  "orderVerified": true,
  "notes": "All items verified and secured"
}
```

**Request Fields:**
- `orderId` (required) - Order ID to confirm pickup
- `bikerId` (required) - Your biker ID
- `verificationCode` (optional) - Restaurant verification code
- `orderVerified` (optional) - Boolean confirming order contents checked
- `notes` (optional) - Any notes about the order

**Response:**
```json
{
  "orderId": 101,
  "orderNumber": "ORD-20250930-101",
  "orderStatus": "PICKED_UP",
  "bikerId": 1,
  "bikerName": "John Biker",
  "customerName": "Jane Doe",
  "customerPhone": "+1234567890",
  "deliveryAddress": "456 Oak Avenue, Apartment 3B, Cityville, 12345",
  "orderAmount": 38.47,
  "pickedUpAt": "2025-09-30T12:15:00",
  "estimatedDeliveryMinutes": 20,
  "message": "Pickup confirmed. Navigate to customer location and complete delivery.",
  "navigationUrl": "https://www.google.com/maps/dir/?api=1&destination=456+Oak+Avenue,+Apartment+3B,+Cityville,+12345"
}
```

---

## What Happens

### 1. Order Status Updates
- ✅ Order status changes: `READY` → `PICKED_UP`
- ✅ `pickedUpAt` timestamp recorded
- ✅ Biker's `lastActive` updated

### 2. Customer Notifications (🔔 Multi-Channel)

**Email:**
- Subject: "Your Order is On The Way!"
- Content includes:
  - Biker name and contact
  - Vehicle details
  - Estimated arrival time

**SMS:**
```
Your order #ORD-20250930-101 has been picked up by John Biker 
and is on its way! ETA: 15-20 min. Track: [app link]
```

**Push Notification:**
```json
{
  "title": "Order Picked Up!",
  "body": "Your order is on its way. Estimated arrival: 15-20 minutes.",
  "data": {
    "orderNumber": "ORD-20250930-101",
    "orderId": "101",
    "bikerName": "John Biker",
    "type": "ORDER_PICKED_UP"
  }
}
```

### 3. Restaurant Notification

**Email:**
- Subject: "Order Picked Up - #ORD-20250930-101"
- Confirms successful pickup
- Includes biker details and delivery address

### 4. Biker Response
- Navigation URL to customer address
- Customer contact information
- Estimated delivery time
- Order amount for payment collection (if cash)

---

## Complete Testing Workflow

### Step 1: Biker Accepts Order
```http
POST /api/bikers/acceptDelivery
{
  "orderId": 101,
  "bikerId": 1,
  "estimatedDeliveryMinutes": 25
}
```

### Step 2: Restaurant Prepares Order
```http
POST /api/cashier/markOrderReadyForPickup/101
```

### Step 3: Biker Arrives & Confirms Pickup (🔔 TRIGGERS NOTIFICATIONS!)
```http
POST /api/bikers/confirmPickup
{
  "orderId": 101,
  "bikerId": 1,
  "orderVerified": true,
  "notes": "All items verified and secured"
}
```

### Step 4: Check Console Logs
```
INFO: Biker 1 confirming pickup for order 101
INFO: Biker 1 confirmed pickup for order ORD-20250930-101. Status: PICKED_UP
INFO: Notified customer jane@example.com that order ORD-20250930-101 was picked up
INFO: Sent pickup confirmation notifications for order ORD-20250930-101
```

### Step 5: Navigate to Customer
Use the `navigationUrl` from response to open Google Maps navigation

### Step 6: Complete Delivery
```http
PUT /api/orders/updateOrderStatus/101
{
  "status": "DELIVERED"
}
```

---

## Customer Email Notification

**Subject:** "Your Order is On The Way!"

**Content:**
```
Great news! Your order #ORD-20250930-101 has been picked up and is on its way!

Delivery Person: John Biker
Phone: +1234567890
Vehicle: MOTORCYCLE - ABC-123
Estimated Arrival: 15-20 minutes

You can track your order in real-time through the app.
Your food will arrive hot and fresh!
```

---

## Restaurant Email Notification

**Subject:** "Order Picked Up - #ORD-20250930-101"

**Content:**
```
Order #ORD-20250930-101 has been picked up by John Biker.

Biker: John Biker
Pickup Time: 12:15 PM
Customer: Jane Doe
Delivery Address: 456 Oak Avenue, Apartment 3B

The order is now en route to the customer.
```

---

## Validation Rules

### Pre-Pickup Validations:
- ✅ Biker must exist and be active
- ✅ Order must exist
- ✅ Order must be assigned to this biker
- ✅ Order status must be CONFIRMED or READY
- ✅ Biker must have valid authentication token

### Post-Pickup Actions:
- ✅ Order status updated to PICKED_UP
- ✅ Pickup timestamp recorded
- ✅ Customer notified via email, SMS, and push
- ✅ Restaurant notified via email
- ✅ Navigation URL generated

---

## Error Responses

### Order Not Assigned
```json
{
  "error": "Order is not assigned to this biker",
  "status": 400
}
```

### Order Not Ready
```json
{
  "error": "Order is not ready for pickup. Current status: PREPARING",
  "status": 400
}
```

### Biker Not Found
```json
{
  "error": "Biker not found with id: 1",
  "status": 404
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

## Navigation Integration

The response includes a Google Maps navigation URL:
```
https://www.google.com/maps/dir/?api=1&destination=456+Oak+Avenue
```

### Mobile App Integration:
```javascript
// Open navigation in Google Maps app
const navigationUrl = response.navigationUrl;
window.location.href = navigationUrl;

// Or use deep linking
if (platform === 'ios') {
  window.location.href = 'comgooglemaps://?daddr=' + encodedAddress;
} else if (platform === 'android') {
  window.location.href = 'google.navigation:q=' + encodedAddress;
}
```

---

## Best Practices

### For Bikers:
1. **Verify Order Contents**
   - Check all items against order list
   - Verify special instructions
   - Ensure hot items are secured properly
   - Check for utensils, condiments, drinks

2. **Communicate Issues**
   - If items missing, notify restaurant immediately
   - Update order notes if substitutions made
   - Contact customer if delays expected

3. **Secure Transportation**
   - Use insulated bag for hot items
   - Keep order level and secure
   - Separate hot and cold items

4. **Safety First**
   - Follow traffic rules
   - Use navigation safely
   - Park legally at pickup/dropoff

### For Restaurants:
1. **Verify Biker Identity**
   - Check order number matches
   - Verify biker name and ID
   - Use verification codes if available

2. **Order Readiness**
   - Only mark READY when fully prepared
   - Include all items and extras
   - Secure packaging for transport

3. **Communication**
   - Inform biker of special handling
   - Mention fragile items
   - Provide customer contact if needed

---

## Order Status Flow

```
PLACED
  ↓
CONFIRMED (Restaurant accepts + Biker notified)
  ↓
PREPARING (Kitchen working)
  ↓
READY (Order ready for pickup)
  ↓
PICKED_UP (Biker confirms pickup) ← 🔔 Customer notified!
  ↓
DELIVERED (Biker completes delivery)
```

---

## Postman Testing

### Environment Variables
```json
{
  "baseUrl": "http://localhost:8085",
  "bikerToken": "",
  "bikerId": "1",
  "orderId": "101"
}
```

### Test Script (Tests tab)
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    console.log("✅ Pickup confirmed!");
    console.log("Order Status:", jsonData.orderStatus);
    console.log("Customer:", jsonData.customerName);
    console.log("Customer Phone:", jsonData.customerPhone);
    console.log("Delivery Address:", jsonData.deliveryAddress);
    console.log("Navigation URL:", jsonData.navigationUrl);
    console.log("ETA:", jsonData.estimatedDeliveryMinutes, "minutes");
    
    // Open navigation URL in browser
    console.log("\n🗺️ Open navigation:");
    console.log(jsonData.navigationUrl);
}
```

---

## cURL Command

```bash
curl -X POST http://localhost:8085/api/bikers/confirmPickup \
  -H "Authorization: Bearer {biker_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 101,
    "bikerId": 1,
    "orderVerified": true,
    "notes": "All items verified"
  }'
```

---

## Testing Scenarios

### Scenario 1: Successful Pickup
1. Order status: READY
2. Biker arrives at restaurant
3. Verifies all items
4. Confirms pickup → Status: PICKED_UP
5. Customer receives notifications
6. Navigate to customer

### Scenario 2: Order Not Ready
1. Order status: PREPARING
2. Biker tries to confirm pickup
3. Receives error: "Order is not ready for pickup"
4. Wait for restaurant to mark READY

### Scenario 3: Wrong Biker
1. Order assigned to Biker A
2. Biker B tries to pickup
3. Receives error: "Order is not assigned to this biker"

---

## Integration with Mobile App

### Recommended UI Flow:

```
1. Biker sees "Navigate to Restaurant" button
   ↓
2. Arrives at restaurant
   ↓
3. Shows "Confirm Pickup" screen with:
   - Order items checklist
   - Verification code field
   - "Verify & Confirm" button
   ↓
4. Clicks "Verify & Confirm"
   ↓
5. App calls confirmPickup API
   ↓
6. Success screen with:
   - Customer details
   - Delivery address
   - "Navigate to Customer" button
   ↓
7. Clicks "Navigate to Customer"
   ↓
8. Opens Google Maps navigation
```

---

## Related Documentation

- **BIKER_ACCEPTANCE_GUIDE.md** - Accepting delivery orders
- **COMPLETE_TESTING_GUIDE.md** - Full system testing
- **BIKER_NOTIFICATION_SYSTEM.md** - Notification architecture

---

## Quick Reference

| Action | Endpoint | Method |
|--------|----------|--------|
| Accept delivery | `/api/bikers/acceptDelivery` | POST |
| View active orders | `/api/bikers/{bikerId}/activeOrders` | GET |
| **Confirm pickup** | `/api/bikers/confirmPickup` | **POST** |
| Mark delivered | `/api/orders/updateOrderStatus/{orderId}` | PUT |

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Order not ready" error | Wait for restaurant to mark order READY |
| "Not assigned to biker" | Ensure you accepted the order first |
| Navigation URL not working | Try copying URL to browser manually |
| Customer not receiving notifications | Check customer email/phone in database |

---

**Last Updated:** 2025-09-30  
**Version:** 1.0
