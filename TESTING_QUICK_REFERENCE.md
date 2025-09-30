# Testing Quick Reference Card

## 🚀 Base URL
```
http://localhost:8085
```

## 🔑 Quick Login Credentials

| Role | Email | Endpoint |
|------|-------|----------|
| Customer | customer@example.com | POST /api/auth/login |
| Cashier | cashier@restaurant.com | POST /api/auth/login |
| Biker | biker@mozfood.com | POST /api/auth/login |
| Admin | admin@restaurant.com | POST /api/auth/login |

## 📝 Complete Order Flow (5 Minutes)

### 1. Login as Customer
```http
POST /api/auth/login
{"email": "customer@example.com", "password": "password123"}
```
→ Save token

### 2. Create Order
```http
POST /api/orders/createOrder
Authorization: Bearer {customer_token}
{
  "customerId": 1,
  "restaurantId": 1,
  "deliveryAddress": "456 Oak Ave",
  "paymentMethod": "CARD",
  "orderItems": [{"menuItemId": 1, "quantity": 2}]
}
```
→ Save `orderId`

### 3. Login as Cashier
```http
POST /api/auth/login
{"email": "cashier@restaurant.com", "password": "cashier123"}
```
→ Save token

### 4. Accept Order (🔔 TRIGGERS NOTIFICATIONS!)
```http
POST /api/cashier/acceptOrder/{orderId}?estimatedPrepTime=30
Authorization: Bearer {cashier_token}
```
→ Check console for biker notifications

### 5. Mark Preparing
```http
PUT /api/cashier/updateOrderStatus
{"orderId": 101, "status": "PREPARING"}
```

### 6. Mark Ready
```http
POST /api/cashier/markOrderReadyForPickup/{orderId}
```

### 7. Assign Biker
```http
POST /api/cashier/assignToDelivery/{orderId}?bikerId=1
```

### 8. Confirm Pickup
```http
POST /api/cashier/confirmOrderDispatch/{orderId}
```

### 9. Biker Accepts Delivery (🔔 NOTIFIES CUSTOMER!)
```http
POST /api/bikers/acceptDelivery
Authorization: Bearer {biker_token}
{
  "orderId": 101,
  "bikerId": 1,
  "estimatedDeliveryMinutes": 25
}
```

### 10. Biker Confirms Pickup (🔔 NOTIFIES CUSTOMER!)
```http
POST /api/bikers/confirmPickup
Authorization: Bearer {biker_token}
{
  "orderId": 101,
  "bikerId": 1,
  "orderVerified": true
}
```

### 11. Track Order (as Customer)
```http
GET /api/orders/{orderId}/track
Authorization: Bearer {customer_token}
```

### 12. Biker Confirms Delivery (🔔 NOTIFIES CUSTOMER!)
```http
POST /api/bikers/confirmDelivery
Authorization: Bearer {biker_token}
{
  "orderId": 101,
  "bikerId": 1,
  "recipientName": "Jane Doe",
  "contactlessDelivery": false
}
```

### 13. View Analytics (as Admin)
```http
GET /api/analytics/getSalesReport?restaurantId=1&startDate=2025-09-01&endDate=2025-09-30&period=DAILY
Authorization: Bearer {admin_token}
```

## 🔔 Notification Trigger Points

| Action | Status Change | Notifications Sent To |
|--------|--------------|----------------------|
| Cashier accepts order | PLACED → CONFIRMED | All available bikers |
| Assign specific biker | N/A | Assigned biker only |
| Biker accepts delivery | N/A | Customer & Restaurant |
| Biker rejects delivery | N/A | Restaurant (& rebroadcast to bikers) |
| Biker confirms pickup | READY → PICKED_UP | Customer (email, SMS, push) & Restaurant |
| Biker confirms delivery | PICKED_UP → DELIVERED | Customer (email, push) & Restaurant |

## 📊 Order Status Progression

```
PLACED → CONFIRMED → PREPARING → READY → PICKED_UP → DELIVERED
         ↑(notify)               ↑(notify)
```

## ✅ Expected Console Output

After accepting order:
```
INFO: Broadcasting order ORD-20250930-101 to all available bikers
INFO: Found 3 available bikers
INFO: Sending push notification to user: 1
INFO: Sending SMS to: +1234567890
INFO: Sending email to: biker@mozfood.com
INFO: Sent new order notification to biker 1
```

## 🛠️ Postman Environment Variables

```
baseUrl = http://localhost:8085
customerToken = (from login)
cashierToken = (from login)
bikerToken = (from login)
adminToken = (from login)
orderId = (from create order)
customerId = 1
restaurantId = 1
bikerId = 1
```

## 🐛 Quick Fixes

| Issue | Solution |
|-------|----------|
| No notifications | Check bikers: `is_online=true`, `is_available=true` |
| 403 Forbidden | Use correct role's token |
| Order not found | Verify orderId exists |
| No available bikers | Insert test biker data |

## 📄 Related Documents

- `COMPLETE_TESTING_GUIDE.md` - Full detailed guide
- `BIKER_ACCEPTANCE_GUIDE.md` - Biker delivery acceptance feature
- `PICKUP_CONFIRMATION_GUIDE.md` - Pickup confirmation at restaurant
- `NAVIGATION_DELIVERY_GUIDE.md` - Navigation & real-time tracking
- `DELIVERY_CONFIRMATION_GUIDE.md` - Delivery confirmation with proof
- `CUSTOMER_INTERACTION_GUIDE.md` - Professional service guidelines
- `BIKER_FEATURES_SUMMARY.md` - Complete biker features overview
- `BIKER_NOTIFICATION_SYSTEM.md` - System architecture
