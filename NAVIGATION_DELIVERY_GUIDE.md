# Navigation & Delivery Guide

## Overview
Complete guide for biker navigation and delivery features with real-time location tracking, route optimization, and efficient delivery to customer addresses.

Base URL: `http://localhost:8085`

---

## Features

✅ **Real-time Location Tracking** - Update biker location continuously  
✅ **Smart Navigation** - Get optimal routes to restaurant and customer  
✅ **Multiple Navigation Apps** - Google Maps & Waze integration  
✅ **Live ETA Updates** - Calculate and update estimated arrival times  
✅ **Customer Tracking** - Customers see biker location in real-time  
✅ **Delivery Tracking** - Complete order status history  

---

## API Endpoints

### 1. Update Biker Location

Continuously update biker's GPS location during delivery.

```http
POST /api/bikers/updateLocation
Authorization: Bearer {biker_token}
Content-Type: application/json

{
  "bikerId": 1,
  "latitude": 40.7128,
  "longitude": -74.0060,
  "speed": 25.5,
  "heading": 180.0,
  "accuracy": 10.0
}
```

**Request Fields:**
- `bikerId` (required) - Biker ID
- `latitude` (required) - Current latitude
- `longitude` (required) - Current longitude
- `speed` (optional) - Current speed in km/h
- `heading` (optional) - Direction in degrees (0-360)
- `accuracy` (optional) - GPS accuracy in meters

**Response:**
```
"Location updated successfully"
```

**What Happens:**
1. ✅ Biker's location updated in database
2. ✅ If order status is PICKED_UP, customer is notified
3. ✅ ETA recalculated based on distance and speed
4. ✅ Real-time tracking updates sent to customer

**Frequency:** Update every 5-10 seconds while en route

---

### 2. Get Navigation Details

Get complete navigation details including route, distance, and ETA.

```http
POST /api/bikers/getNavigation
Authorization: Bearer {biker_token}
Content-Type: application/json

{
  "orderId": 101,
  "bikerId": 1,
  "navigationType": "CUSTOMER"
}
```

**Navigation Types:**
- `"RESTAURANT"` - Navigate to restaurant for pickup
- `"CUSTOMER"` - Navigate to customer for delivery

**Response:**
```json
{
  "orderId": 101,
  "orderNumber": "ORD-20250930-101",
  "destinationType": "CUSTOMER",
  "destinationAddress": "456 Oak Avenue, Apartment 3B",
  "destinationLatitude": 40.7589,
  "destinationLongitude": -73.9851,
  "currentLatitude": 40.7128,
  "currentLongitude": -74.0060,
  "distanceKm": 3.5,
  "estimatedTimeMinutes": 15,
  "navigationUrl": "https://www.google.com/maps/dir/?api=1&destination=456+Oak+Avenue",
  "googleMapsUrl": "https://www.google.com/maps/dir/?api=1&destination=456+Oak+Avenue",
  "wazeUrl": "https://waze.com/ul?q=456+Oak+Avenue&navigate=yes",
  "routeInfo": {
    "totalDistanceKm": 3.5,
    "totalTimeMinutes": 15,
    "summary": "Fastest route via main roads",
    "warnings": "Traffic may affect arrival time"
  }
}
```

---

### 3. Start Navigation (Quick Access)

Quick GET endpoint to start navigation.

```http
GET /api/bikers/{bikerId}/navigation/{orderId}?destinationType=CUSTOMER
Authorization: Bearer {biker_token}
```

**Example:**
```http
GET /api/bikers/1/navigation/101?destinationType=CUSTOMER
```

**Query Parameters:**
- `destinationType` (optional) - RESTAURANT or CUSTOMER (default: CUSTOMER)

**Response:** Same as getNavigation endpoint

---

### 4. Get Delivery Tracking (Customer View)

Customers and support can track order delivery in real-time.

```http
GET /api/bikers/tracking/{orderId}
```

**Example:**
```http
GET /api/bikers/tracking/101
```

**Response:**
```json
{
  "trackingId": null,
  "orderId": "101",
  "currentStatus": "PICKED_UP",
  "currentStatusTime": null,
  "deliveryPersonId": "1",
  "deliveryPersonName": "John Biker",
  "deliveryPersonPhone": "+1234567890",
  "currentLatitude": 40.7128,
  "currentLongitude": -74.0060,
  "currentLocationName": null,
  "estimatedDeliveryTime": null,
  "statusHistory": [
    {
      "status": "PLACED",
      "timestamp": "2025-09-30T11:30:00",
      "locationName": null,
      "notes": "Order placed by customer",
      "latitude": null,
      "longitude": null
    },
    {
      "status": "CONFIRMED",
      "timestamp": "2025-09-30T11:31:00",
      "locationName": null,
      "notes": "Restaurant confirmed order",
      "latitude": null,
      "longitude": null
    },
    {
      "status": "PICKED_UP",
      "timestamp": "2025-09-30T12:15:00",
      "locationName": null,
      "notes": "Biker picked up order",
      "latitude": null,
      "longitude": null
    }
  ],
  "delivered": false,
  "deliveredAt": null,
  "deliveryProofImage": null,
  "recipientName": null,
  "recipientSignature": null,
  "notes": null
}
```

---

## Complete Navigation Workflow

### Step 1: Accept Delivery
```http
POST /api/bikers/acceptDelivery
{
  "orderId": 101,
  "bikerId": 1
}
```

### Step 2: Navigate to Restaurant
```http
GET /api/bikers/1/navigation/101?destinationType=RESTAURANT
```

**Response includes:**
- Google Maps URL to restaurant
- Waze URL to restaurant
- Distance and ETA

### Step 3: Update Location En Route
```http
POST /api/bikers/updateLocation
{
  "bikerId": 1,
  "latitude": 40.7200,
  "longitude": -74.0050
}
```

**Repeat every 5-10 seconds while moving**

### Step 4: Arrive & Confirm Pickup
```http
POST /api/bikers/confirmPickup
{
  "orderId": 101,
  "bikerId": 1,
  "orderVerified": true
}
```

### Step 5: Navigate to Customer
```http
GET /api/bikers/1/navigation/101?destinationType=CUSTOMER
```

**Response includes:**
- Google Maps URL to customer
- Customer address and phone
- Distance and ETA

### Step 6: Update Location During Delivery
```http
POST /api/bikers/updateLocation
{
  "bikerId": 1,
  "latitude": 40.7500,
  "longitude": -73.9900
}
```

**Customer receives real-time updates!**

### Step 7: Arrive & Complete Delivery
```http
PUT /api/orders/updateOrderStatus/101
{
  "status": "DELIVERED"
}
```

---

## Navigation Integration

### Google Maps Deep Link

**iOS:**
```javascript
const googleMapsUrl = `comgooglemaps://?daddr=${latitude},${longitude}`;
window.location.href = googleMapsUrl;
```

**Android:**
```javascript
const googleMapsUrl = `google.navigation:q=${latitude},${longitude}`;
window.location.href = googleMapsUrl;
```

**Web/Fallback:**
```javascript
const googleMapsUrl = `https://www.google.com/maps/dir/?api=1&destination=${address}`;
window.open(googleMapsUrl, '_blank');
```

### Waze Deep Link

```javascript
const wazeUrl = `waze://?q=${address}&navigate=yes`;
window.location.href = wazeUrl;

// Fallback to web
setTimeout(() => {
  window.location.href = `https://waze.com/ul?q=${address}&navigate=yes`;
}, 500);
```

---

## Real-Time Location Updates

### Recommended Implementation

```javascript
// Start location tracking when order picked up
function startLocationTracking(bikerId, orderId) {
  if (!navigator.geolocation) {
    console.error('Geolocation not supported');
    return;
  }
  
  const watchId = navigator.geolocation.watchPosition(
    (position) => {
      updateBikerLocation({
        bikerId: bikerId,
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        speed: position.coords.speed * 3.6, // m/s to km/h
        heading: position.coords.heading,
        accuracy: position.coords.accuracy
      });
    },
    (error) => {
      console.error('Location error:', error);
    },
    {
      enableHighAccuracy: true,
      timeout: 5000,
      maximumAge: 0
    }
  );
  
  return watchId;
}

function updateBikerLocation(locationData) {
  fetch('http://localhost:8085/api/bikers/updateLocation', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(locationData)
  })
  .then(response => response.text())
  .then(data => console.log('Location updated:', data))
  .catch(error => console.error('Error:', error));
}

// Stop tracking when delivered
function stopLocationTracking(watchId) {
  navigator.geolocation.clearWatch(watchId);
}
```

---

## Customer Tracking UI

### Recommended Features

1. **Live Map** - Show biker's current location on map
2. **Route Line** - Draw route from biker to customer
3. **ETA Display** - Show estimated arrival time
4. **Status Updates** - Show order status changes
5. **Contact Buttons** - Call/message biker directly

### Sample Customer View

```javascript
// Poll for updates every 10 seconds
setInterval(() => {
  fetch(`http://localhost:8085/api/bikers/tracking/${orderId}`)
    .then(response => response.json())
    .then(data => {
      updateMapMarker(data.currentLatitude, data.currentLongitude);
      updateETA(data.estimatedDeliveryTime);
      updateStatus(data.currentStatus);
    });
}, 10000);
```

---

## Distance & ETA Calculation

### Haversine Formula (Straight-line distance)

```java
private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int EARTH_RADIUS_KM = 6371;
    
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
               Math.sin(dLon / 2) * Math.sin(dLon / 2);
    
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    
    return EARTH_RADIUS_KM * c;
}
```

### ETA Calculation

```java
private int calculateETA(double distanceKm, double averageSpeedKmh) {
    // Average urban delivery speed: 20-30 km/h
    if (averageSpeedKmh == 0) {
        averageSpeedKmh = 25.0; // Default speed
    }
    
    double timeHours = distanceKm / averageSpeedKmh;
    int timeMinutes = (int) Math.ceil(timeHours * 60);
    
    // Add buffer for stops, traffic, etc.
    timeMinutes += 5;
    
    return timeMinutes;
}
```

---

## Validation Rules

### Update Location:
- ✅ Biker must exist
- ✅ Valid GPS coordinates required
- ✅ Latitude: -90 to 90
- ✅ Longitude: -180 to 180

### Get Navigation:
- ✅ Biker must exist and be active
- ✅ Order must exist
- ✅ Order must be assigned to biker
- ✅ Valid destination type

---

## Console Logs

### Location Update:
```
INFO: Updating location for biker 1: lat=40.7128, lon=-74.0060
INFO: Biker 1 location update for order ORD-20250930-101: lat=40.7128, lon=-74.0060
INFO: Customer tracking update sent for order ORD-20250930-101
INFO: Location updated for biker 1
```

### Navigation Start:
```
INFO: Starting navigation for biker 1 to CUSTOMER for order 101
INFO: Getting navigation for biker 1 and order 101
INFO: Navigation started for biker 1 to CUSTOMER: distance=3.5km, ETA=15min
```

---

## Testing Scenarios

### Scenario 1: Restaurant to Customer

1. Accept order
2. Start navigation to restaurant
3. Update location while traveling
4. Confirm pickup at restaurant
5. Start navigation to customer
6. Update location during delivery
7. Complete delivery

### Scenario 2: Location Tracking

```bash
# Accept order
curl -X POST http://localhost:8085/api/bikers/acceptDelivery \
  -H "Authorization: Bearer {token}" \
  -d '{"orderId":101,"bikerId":1}'

# Confirm pickup
curl -X POST http://localhost:8085/api/bikers/confirmPickup \
  -H "Authorization: Bearer {token}" \
  -d '{"orderId":101,"bikerId":1,"orderVerified":true}'

# Update location (repeat)
curl -X POST http://localhost:8085/api/bikers/updateLocation \
  -H "Authorization: Bearer {token}" \
  -d '{"bikerId":1,"latitude":40.7128,"longitude":-74.0060,"speed":25.5}'

# Customer tracks
curl http://localhost:8085/api/bikers/tracking/101
```

---

## Production Recommendations

### 1. Google Maps Distance Matrix API

Replace mock distance/ETA calculations:

```java
// Integration with Google Maps
String apiUrl = String.format(
    "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%f,%f&destinations=%s&key=%s",
    currentLat, currentLon, destinationAddress, apiKey
);

// Parse response for accurate distance and duration
```

### 2. WebSocket for Real-Time Updates

Replace polling with WebSocket:

```java
@SendTo("/topic/orders/{orderId}/location")
public LocationUpdate sendLocationUpdate(LocationUpdateRequest location) {
    return new LocationUpdate(location);
}
```

### 3. Background Location Tracking

Mobile app should track location in background:

```javascript
// React Native example
BackgroundGeolocation.configure({
  desiredAccuracy: BackgroundGeolocation.HIGH_ACCURACY,
  stationaryRadius: 50,
  distanceFilter: 50,
  interval: 10000, // 10 seconds
  fastestInterval: 5000
});
```

### 4. Battery Optimization

- Use adaptive update frequency
- Reduce updates when stationary
- Stop tracking after delivery

---

## Performance Optimization

### 1. Caching
- Cache Google Maps API responses
- Store recent routes locally
- Cache navigation URLs

### 2. Batch Updates
- Group location updates if needed
- Send delta updates only

### 3. Database Indexing
```sql
CREATE INDEX idx_bikers_location ON bikers(current_latitude, current_longitude);
CREATE INDEX idx_orders_status ON orders(order_status);
```

---

## Error Handling

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| GPS not available | Device location off | Prompt user to enable |
| Low accuracy | Poor GPS signal | Wait for better signal |
| Network error | Connection lost | Queue updates, retry |
| Invalid coordinates | Out of range values | Validate before sending |

---

## Security Considerations

1. **Location Privacy**
   - Only share location with customers for active orders
   - Stop tracking after delivery
   - Don't store historical location data

2. **Authentication**
   - All endpoints require BIKER role
   - Verify biker owns the order

3. **Rate Limiting**
   - Limit location updates to max 1 per second
   - Prevent spam/abuse

---

## Related Documentation

- **BIKER_ACCEPTANCE_GUIDE.md** - Delivery acceptance
- **PICKUP_CONFIRMATION_GUIDE.md** - Pickup confirmation
- **COMPLETE_TESTING_GUIDE.md** - Full system testing
- **BIKER_FEATURES_SUMMARY.md** - All biker features

---

## Quick Reference

| Feature | Endpoint | Method |
|---------|----------|--------|
| Update location | `/api/bikers/updateLocation` | POST |
| Get navigation | `/api/bikers/getNavigation` | POST |
| Start navigation | `/api/bikers/{bikerId}/navigation/{orderId}` | GET |
| Track delivery | `/api/bikers/tracking/{orderId}` | GET |

---

**Version:** 1.0  
**Last Updated:** 2025-09-30  
**Status:** ✅ Implemented (Mock calculations - integrate Google Maps API for production)
