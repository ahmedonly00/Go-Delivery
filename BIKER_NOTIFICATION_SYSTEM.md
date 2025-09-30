# Biker Notification System Documentation

## Overview
This document describes the notification system implemented for bikers (delivery personnel) in the MozFood/GoDelivery application. Bikers are the frontline workforce responsible for ensuring timely and safe delivery of meals.

## Key Responsibilities

### Order Notification
Bikers receive notifications of accepted orders from restaurants via multiple channels:
- **Push Notifications** - Real-time alerts on mobile devices
- **SMS** - Text message alerts with order summary
- **Email** - Detailed order information

## System Architecture

### Components

#### 1. NotificationService
**Location:** `src/main/java/com/goDelivery/goDelivery/service/NotificationService.java`

**Key Methods:**
- `notifyBikerNewOrder()` - Sends multi-channel notifications to a specific biker
  - Parameters: bikerId, bikerEmail, bikerPhone, orderNumber, restaurantName, pickupAddress, deliveryAddress
  - Sends push notification, SMS, and email
  
- `notifyAvailableBikersForOrder()` - Broadcasts order availability (placeholder for future implementation)

#### 2. BikerService
**Location:** `src/main/java/com/goDelivery/goDelivery/service/BikerService.java`

**Key Methods:**
- `findAvailableBikers()` - Returns list of online, available, and active bikers
- `findNearestAvailableBiker()` - Finds closest biker using Haversine formula
- `broadcastOrderToAvailableBikers()` - Notifies all available bikers about a new order
- `assignBikerToOrder()` - Assigns a biker to an order and sends notification
- `calculateDistance()` - Calculates distance between two GPS coordinates

#### 3. OrderStatusUpdateService
**Location:** `src/main/java/com/goDelivery/goDelivery/service/OrderStatusUpdateService.java`

**Key Methods:**
- `updateOrderStatusWithNotification()` - Updates order status and triggers notifications
- `sendStatusUpdateNotification()` - Routes notifications to customers and bikers
- `notifyBikersForNewOrder()` - Determines whether to notify assigned biker or broadcast to all

#### 4. CashierService
**Location:** `src/main/java/com/goDelivery/goDelivery/service/CashierService.java`

**Key Methods:**
- `acceptOrder()` - Restaurant accepts order and triggers status change to CONFIRMED
- This automatically triggers biker notifications through OrderStatusUpdateService

## Workflow

### Order Acceptance and Notification Flow

```
1. Restaurant receives new order (Status: PLACED)
   │
   ↓
2. Cashier accepts order via CashierService.acceptOrder()
   │
   ↓
3. Order status changes to CONFIRMED
   │
   ↓
4. OrderStatusUpdateService detects CONFIRMED status
   │
   ↓
5. System checks if biker is already assigned
   │
   ├─→ YES: Notify specific biker via BikerService.assignBikerToOrder()
   │
   └─→ NO: Broadcast to all available bikers via BikerService.broadcastOrderToAvailableBikers()
   │
   ↓
6. NotificationService sends multi-channel notifications
   ├─→ Push Notification (with order details as payload)
   ├─→ SMS (brief summary)
   └─→ Email (detailed information)
```

### Order Status Flow

```
PLACED → CONFIRMED → PREPARING → READY → PICKED_UP → DELIVERED
         ↑
         └─ Biker notified here
```

## Notification Details

### Push Notification Payload
```json
{
  "title": "New Order Available!",
  "message": "New order #ORD123 is ready for pickup!\nRestaurant: Pizza Palace\nPickup: 123 Main St\nDelivery: 456 Oak Ave\n\nPlease confirm acceptance in the app.",
  "data": {
    "orderNumber": "ORD123",
    "restaurantName": "Pizza Palace",
    "pickupAddress": "123 Main St",
    "deliveryAddress": "456 Oak Ave",
    "type": "NEW_ORDER"
  }
}
```

### SMS Format
```
New order #ORD123 ready at Pizza Palace. Check app for details.
```

### Email Format
- Subject: "New Delivery Order #ORD123"
- Template: "biker-new-order"
- Contains full order details, addresses, and instructions

## Biker Selection Logic

### Current Implementation
1. **Check for assigned biker** - If order already has a biker assigned, notify them directly
2. **Broadcast to available bikers** - If no biker assigned, send notifications to all available bikers

### Future Enhancements
The system is designed to support:
- **Distance-based selection** - Using `findNearestAvailableBiker()` with GPS coordinates
- **Load balancing** - Distribute orders evenly among bikers
- **Performance-based selection** - Prioritize bikers with higher ratings
- **First-come-first-served** - Allow bikers to accept orders on a competitive basis

## Configuration

### Biker Availability Criteria
A biker is considered available if:
- `isOnline = true` - Biker is actively using the app
- `isAvailable = true` - Biker is not currently on a delivery
- `isActive = true` - Biker account is active and in good standing

### Notification Channels
All three channels (Push, SMS, Email) are sent simultaneously to ensure bikers receive the notification through their preferred method.

## Database Schema

### Bikers Table Key Fields
- `biker_id` - Unique identifier
- `email` - For email notifications
- `phone_number` - For SMS notifications
- `current_latitude` - For distance calculations
- `current_longitude` - For distance calculations
- `is_available` - Availability status
- `is_online` - Online status
- `is_active` - Account status
- `rating` - Biker performance rating
- `total_deliveries` - Total number of deliveries completed

### Order Table Key Fields
- `order_id` - Unique identifier
- `order_number` - Human-readable order number
- `order_status` - Current status (PLACED, CONFIRMED, etc.)
- `biker_id` - Assigned biker (nullable)
- `order_confirmed_at` - Timestamp when restaurant accepted
- `delivery_address` - Customer delivery location

## API Integration Points

### For Mobile App Development
The biker mobile app should:

1. **Listen for push notifications** with type "NEW_ORDER"
2. **Display order details** from the notification payload
3. **Provide "Accept Order" button** to confirm delivery
4. **Update biker location** regularly for distance calculations
5. **Update availability status** when on/off duty

### Recommended Endpoints (to be implemented)
```
POST /api/bikers/{bikerId}/accept-order/{orderId}
POST /api/bikers/{bikerId}/reject-order/{orderId}
PUT  /api/bikers/{bikerId}/location
PUT  /api/bikers/{bikerId}/availability
GET  /api/bikers/{bikerId}/active-orders
```

## Error Handling

The notification system includes comprehensive error handling:
- Failed notifications are logged but don't block order processing
- Each biker notification is wrapped in try-catch to prevent one failure from affecting others
- System continues operating even if notification services are temporarily unavailable

## Testing Recommendations

1. **Unit Tests**
   - Test biker availability filtering
   - Test distance calculation accuracy
   - Test notification content generation

2. **Integration Tests**
   - Test order acceptance flow
   - Test notification delivery to multiple bikers
   - Test assigned biker notification

3. **Load Tests**
   - Test broadcasting to 100+ available bikers
   - Test concurrent order acceptances

## Future Enhancements

1. **Smart Assignment Algorithm**
   - Implement AI-based biker assignment
   - Consider traffic conditions
   - Optimize for delivery time and cost

2. **Real-time Tracking**
   - WebSocket notifications for instant updates
   - Live order feed for available bikers

3. **Order Acceptance Window**
   - Time-limited order acceptance
   - Automatic reassignment if not accepted

4. **Performance Metrics**
   - Track notification delivery success rate
   - Monitor average acceptance time
   - Analyze biker response patterns

## Support and Maintenance

### Logs
All notification events are logged with INFO level:
```
INFO: Sent new order notification to biker {bikerId} for order {orderNumber}
INFO: Broadcasting order {orderNumber} to all available bikers
INFO: Found {count} available bikers
```

### Error Logs
Failures are logged with ERROR level:
```
ERROR: Failed to notify bikers for order {orderId}: {errorMessage}
ERROR: Failed to notify biker {bikerId} for order {orderNumber}: {errorMessage}
```

### Monitoring Points
- Number of available bikers at any time
- Notification success/failure rate
- Average time from order confirmation to biker acceptance
- Biker response rate to notifications

## Version History
- **v1.0** (2025-09-30) - Initial implementation of biker notification system
  - Multi-channel notifications (Push, SMS, Email)
  - Broadcast to available bikers
  - Distance-based biker selection support
  - Integration with order status workflow
