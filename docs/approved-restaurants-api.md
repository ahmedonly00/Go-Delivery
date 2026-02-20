# Get Approved Nearby Restaurants — API Documentation

## Overview

This endpoint allows customers to discover **approved restaurants** near their GPS location. It supports geo-radius filtering, cuisine/rating/delivery-fee filters, multiple sort orders, and pagination.

---

## Endpoint

```
GET /api/restaurants/approved
```

- **Auth required:** No (public endpoint)
- **Controller:** `RestaurantController` → `getApprovedRestaurants()`
- **Service:** `RestaurantService.findNearbyApprovedRestaurants()` → `GeoLocationService.findNearbyRestaurants()`

---

## Query Parameters

| Parameter        | Type    | Required | Default    | Description |
|-----------------|---------|----------|------------|-------------|
| `latitude`      | double  | ✅ Yes   | —          | Customer's latitude. Must be between **-90** and **90**. |
| `longitude`     | double  | ✅ Yes   | —          | Customer's longitude. Must be between **-180** and **180**. |
| `radiusKm`      | double  | No       | `10.0`     | Search radius in kilometres. Must be between **0** and **50**. |
| `page`          | int     | No       | `0`        | Page number (0-indexed). |
| `size`          | int     | No       | `20`       | Number of results per page. |
| `sortBy`        | string  | No       | `distance` | Sort order. Accepted values: `distance`, `rating`, `popularity`. |
| `cuisineType`   | string  | No       | —          | Filter by cuisine type (case-insensitive). e.g. `Italian`, `Seafood`. |
| `minRating`     | double  | No       | —          | Only return restaurants with `rating >= minRating`. |
| `maxDeliveryFee`| float   | No       | —          | Only return restaurants with `deliveryFee <= maxDeliveryFee`. |

---

## How It Works (Processing Pipeline)

```
Customer GPS (lat/lon)
        │
        ▼
GeoLocationService.findNearbyRestaurants()
   → Fetches APPROVED + ACTIVE restaurants
   → Calculates distance from customer
   → Filters to those within radiusKm
   → Returns list pre-sorted by distance
        │
        ▼
Optional Filters (in-memory)
   → cuisineType (case-insensitive match)
   → minRating
   → maxDeliveryFee
        │
        ▼
Sorting
   → distance  (already sorted by service)
   → rating    (descending)
   → popularity (by totalReviews, descending)
        │
        ▼
Mapping → RestaurantDTO (includes distanceFromUser, distanceDisplay, estimatedDeliveryMinutes)
        │
        ▼
Pagination → Spring Page<RestaurantDTO>
```

---

## Response

### HTTP 200 — Success

Returns a **Spring Page object** containing a list of `RestaurantDTO` items.

```json
{
  "content": [
    {
      "restaurantId": 1,
      "restaurantName": "Café Maputo",
      "location": "Maputo, Mozambique",
      "cuisineType": "African",
      "email": "owner@cafemaputo.mz",
      "phoneNumber": "+258841234567",
      "logoUrl": "/api/files/restaurants/1/logo.png",
      "description": "Authentic Mozambican cuisine.",
      "rating": 4.5,
      "totalReviews": 120,
      "deliveryType": "SYSTEM_DELIVERY",
      "deliveryFee": 50.0,
      "deliveryRadius": null,
      "baseDeliveryFee": null,
      "perKmFee": null,
      "minimumOrderAmount": 200.0,
      "averagePreparationTime": 25,
      "distanceFromUser": 1.3,
      "distanceDisplay": "1.3 km",
      "estimatedDeliveryMinutes": 18,
      "isApproved": true,
      "approvalStatus": "APPROVED",
      "isActive": true,
      "operatingHours": {
        "mondayOpen": "08:00",
        "mondayClose": "22:00"
      },
      "createdAt": "2024-11-01",
      "updatedAt": "2025-01-15"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 5,
  "totalPages": 1,
  "last": true,
  "first": true,
  "empty": false
}
```

### Key Response Fields

| Field | Description |
|-------|-------------|
| `distanceFromUser` | Distance in km between the customer and the restaurant. Only included when non-null. |
| `distanceDisplay` | Human-readable distance such as `"1.3 km"` or `"800 m"`. |
| `estimatedDeliveryMinutes` | Estimated delivery time in minutes based on distance. |
| `deliveryType` | `SYSTEM_DELIVERY` or `SELF_DELIVERY` |
| `deliveryFee` | Legacy flat delivery fee (backward-compatible). |
| `baseDeliveryFee` + `perKmFee` | Used when `deliveryType = SELF_DELIVERY`. |

### HTTP 400 — Validation Errors

```json
{ "error": "Latitude must be between -90 and 90" }
{ "error": "Longitude must be between -180 and 180" }
{ "error": "Radius must be between 0 and 50 km" }
```

---

## How the Client Obtains GPS Coordinates

The backend **does not know** the customer's location on its own — the client app is responsible for reading the device GPS and passing the resulting `latitude` and `longitude` as query parameters. This must happen **before** the API call is made.

### Flow

```
Device GPS Hardware
        │
        ▼
Browser Geolocation API   or   Flutter geolocator package
        │                                │
        └──────────────┬─────────────────┘
                       ▼
         latitude=X & longitude=Y
                       │
                       ▼
    GET /api/restaurants/approved?latitude=X&longitude=Y&...
```

---

### Web — Browser Geolocation API

Modern browsers expose `navigator.geolocation` built-in. No extra library needed.

```typescript
// Step 1 — Check if geolocation is supported
if (!navigator.geolocation) {
  alert('Your browser does not support location access.');
}

// Step 2 — Request permission and get coordinates
function getUserLocation(): Promise<{ latitude: number; longitude: number }> {
  return new Promise((resolve, reject) => {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        });
      },
      (error) => {
        switch (error.code) {
          case error.PERMISSION_DENIED:
            reject(new Error('Location permission denied by user.'));
            break;
          case error.POSITION_UNAVAILABLE:
            reject(new Error('Location information is unavailable.'));
            break;
          case error.TIMEOUT:
            reject(new Error('Location request timed out.'));
            break;
          default:
            reject(new Error('An unknown error occurred.'));
        }
      },
      {
        enableHighAccuracy: true, // Use GPS chip, not just IP/WiFi
        timeout: 10000,           // Give up after 10 seconds
        maximumAge: 60000,        // Accept cached position up to 1 minute old
      }
    );
  });
}

// Step 3 — Use it before calling the API
async function loadNearbyRestaurants() {
  try {
    const { latitude, longitude } = await getUserLocation();
    const data = await getNearbyRestaurants(latitude, longitude);
    // render data...
  } catch (err) {
    console.error('Location error:', err.message);
    // Show fallback UI or ask user to enter location manually
  }
}
```

> **Browser permission prompt**: The first time this runs, the browser will show a popup asking *"Allow this site to know your location?"*. The coordinates are only returned if the user clicks **Allow**.

---

### Flutter — `geolocator` Package

#### 1. Add dependency to `pubspec.yaml`

```yaml
dependencies:
  geolocator: ^12.0.0
```

#### 2. Configure platform permissions

**Android** — `android/app/src/main/AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

**iOS** — `ios/Runner/Info.plist`:
```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>GoDelivery needs your location to show restaurants near you.</string>
```

#### 3. Location helper service

```dart
import 'package:geolocator/geolocator.dart';

class LocationService {
  /// Returns the customer's current position.
  /// Handles permission checks automatically.
  static Future<Position> getCurrentPosition() async {
    // Check if location services are enabled on the device
    bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      throw Exception('Location services are disabled. Please enable GPS.');
    }

    // Check / request permission
    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) {
        throw Exception('Location permission denied.');
      }
    }

    if (permission == LocationPermission.deniedForever) {
      throw Exception(
        'Location permission permanently denied. '
        'Please enable it from Settings.',
      );
    }

    // All good — get the actual position
    return Geolocator.getCurrentPosition(
      desiredAccuracy: LocationAccuracy.high, // Uses GPS chip
      timeLimit: const Duration(seconds: 10),
    );
  }
}
```

#### 4. Use in a screen before calling the API

```dart
Future<void> _fetchRestaurants() async {
  setState(() => _isLoading = true);
  try {
    // Step 1: Get device coordinates
    final Position pos = await LocationService.getCurrentPosition();

    // Step 2: Pass them to the API
    final result = await _api.getNearbyRestaurants(
      latitude: pos.latitude,
      longitude: pos.longitude,
      radiusKm: 10,
      sortBy: 'distance',
    );

    setState(() => _restaurants = result.content);
  } on Exception catch (e) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(e.toString())),
    );
  } finally {
    setState(() => _isLoading = false);
  }
}
```

---

## Frontend Integration (JavaScript / TypeScript)

### Basic Fetch

```typescript
async function getNearbyRestaurants(
  latitude: number,
  longitude: number,
  options?: {
    radiusKm?: number;
    page?: number;
    size?: number;
    sortBy?: 'distance' | 'rating' | 'popularity';
    cuisineType?: string;
    minRating?: number;
    maxDeliveryFee?: number;
  }
) {
  const params = new URLSearchParams({
    latitude: String(latitude),
    longitude: String(longitude),
    radiusKm: String(options?.radiusKm ?? 10),
    page: String(options?.page ?? 0),
    size: String(options?.size ?? 20),
    sortBy: options?.sortBy ?? 'distance',
  });

  if (options?.cuisineType) params.append('cuisineType', options.cuisineType);
  if (options?.minRating != null) params.append('minRating', String(options.minRating));
  if (options?.maxDeliveryFee != null) params.append('maxDeliveryFee', String(options.maxDeliveryFee));

  const response = await fetch(`/api/restaurants/approved?${params.toString()}`);
  if (!response.ok) {
    const err = await response.json();
    throw new Error(err.error ?? 'Failed to fetch restaurants');
  }
  return response.json(); // Spring Page<RestaurantDTO>
}
```

### Example Usage

```typescript
// Get user's location then fetch restaurants
navigator.geolocation.getCurrentPosition(async (position) => {
  const { latitude, longitude } = position.coords;

  const page = await getNearbyRestaurants(latitude, longitude, {
    radiusKm: 5,
    sortBy: 'rating',
    cuisineType: 'African',
    minRating: 4.0,
  });

  console.log(`Found ${page.totalElements} restaurants`);
  page.content.forEach((r) => {
    console.log(`${r.restaurantName} — ${r.distanceDisplay} — ⭐ ${r.rating}`);
  });
});
```

### Infinite Scroll / Load More

```typescript
let currentPage = 0;
const PAGE_SIZE = 20;

async function loadMore(lat: number, lng: number) {
  const data = await getNearbyRestaurants(lat, lng, {
    page: currentPage,
    size: PAGE_SIZE,
  });

  renderRestaurants(data.content);          // append to list
  if (!data.last) currentPage++;           // only increment if more pages exist
}
```

---

## Flutter (Mobile) Integration

### Model Class

```dart
class RestaurantDTO {
  final int restaurantId;
  final String restaurantName;
  final String location;
  final String cuisineType;
  final String email;
  final String phoneNumber;
  final String? logoUrl;
  final String description;
  final double rating;
  final int totalReviews;
  final String deliveryType;
  final double? deliveryFee;
  final double? minimumOrderAmount;
  final int? averagePreparationTime;
  final double? distanceFromUser;
  final String? distanceDisplay;
  final int? estimatedDeliveryMinutes;
  final bool isActive;
  final bool isApproved;

  const RestaurantDTO({
    required this.restaurantId,
    required this.restaurantName,
    required this.location,
    required this.cuisineType,
    required this.email,
    required this.phoneNumber,
    this.logoUrl,
    required this.description,
    required this.rating,
    required this.totalReviews,
    required this.deliveryType,
    this.deliveryFee,
    this.minimumOrderAmount,
    this.averagePreparationTime,
    this.distanceFromUser,
    this.distanceDisplay,
    this.estimatedDeliveryMinutes,
    required this.isActive,
    required this.isApproved,
  });

  factory RestaurantDTO.fromJson(Map<String, dynamic> json) => RestaurantDTO(
        restaurantId: json['restaurantId'],
        restaurantName: json['restaurantName'],
        location: json['location'],
        cuisineType: json['cuisineType'],
        email: json['email'],
        phoneNumber: json['phoneNumber'],
        logoUrl: json['logoUrl'],
        description: json['description'],
        rating: (json['rating'] as num?)?.toDouble() ?? 0.0,
        totalReviews: json['totalReviews'] ?? 0,
        deliveryType: json['deliveryType'] ?? 'SYSTEM_DELIVERY',
        deliveryFee: (json['deliveryFee'] as num?)?.toDouble(),
        minimumOrderAmount: (json['minimumOrderAmount'] as num?)?.toDouble(),
        averagePreparationTime: json['averagePreparationTime'],
        distanceFromUser: (json['distanceFromUser'] as num?)?.toDouble(),
        distanceDisplay: json['distanceDisplay'],
        estimatedDeliveryMinutes: json['estimatedDeliveryMinutes'],
        isActive: json['isActive'] ?? true,
        isApproved: json['isApproved'] ?? false,
      );
}
```

### Paginated Response Wrapper

```dart
class PagedResponse<T> {
  final List<T> content;
  final int totalElements;
  final int totalPages;
  final bool last;
  final bool first;
  final bool empty;

  const PagedResponse({
    required this.content,
    required this.totalElements,
    required this.totalPages,
    required this.last,
    required this.first,
    required this.empty,
  });

  factory PagedResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJsonT,
  ) =>
      PagedResponse(
        content: (json['content'] as List)
            .map((e) => fromJsonT(e as Map<String, dynamic>))
            .toList(),
        totalElements: json['totalElements'] ?? 0,
        totalPages: json['totalPages'] ?? 0,
        last: json['last'] ?? true,
        first: json['first'] ?? true,
        empty: json['empty'] ?? true,
      );
}
```

### API Service

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:geolocator/geolocator.dart';

class RestaurantApiService {
  final String baseUrl;

  const RestaurantApiService({required this.baseUrl});

  Future<PagedResponse<RestaurantDTO>> getNearbyRestaurants({
    required double latitude,
    required double longitude,
    double radiusKm = 10.0,
    int page = 0,
    int size = 20,
    String sortBy = 'distance',       // 'distance' | 'rating' | 'popularity'
    String? cuisineType,
    double? minRating,
    double? maxDeliveryFee,
  }) async {
    final queryParams = {
      'latitude': latitude.toString(),
      'longitude': longitude.toString(),
      'radiusKm': radiusKm.toString(),
      'page': page.toString(),
      'size': size.toString(),
      'sortBy': sortBy,
      if (cuisineType != null) 'cuisineType': cuisineType,
      if (minRating != null) 'minRating': minRating.toString(),
      if (maxDeliveryFee != null) 'maxDeliveryFee': maxDeliveryFee.toString(),
    };

    final uri = Uri.parse('$baseUrl/api/restaurants/approved')
        .replace(queryParameters: queryParams);

    final response = await http.get(uri);

    if (response.statusCode == 200) {
      final json = jsonDecode(response.body) as Map<String, dynamic>;
      return PagedResponse.fromJson(json, RestaurantDTO.fromJson);
    } else if (response.statusCode == 400) {
      final err = jsonDecode(response.body);
      throw Exception(err['error'] ?? 'Invalid request parameters');
    } else {
      throw Exception('Failed to fetch restaurants: ${response.statusCode}');
    }
  }
}
```

### Widget — Restaurants List Screen

```dart
import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';

class NearbyRestaurantsScreen extends StatefulWidget {
  const NearbyRestaurantsScreen({super.key});

  @override
  State<NearbyRestaurantsScreen> createState() => _NearbyRestaurantsScreenState();
}

class _NearbyRestaurantsScreenState extends State<NearbyRestaurantsScreen> {
  final _api = RestaurantApiService(baseUrl: 'https://your-api.godelivery.mz');
  final List<RestaurantDTO> _restaurants = [];
  int _page = 0;
  bool _isLoading = false;
  bool _hasMore = true;

  @override
  void initState() {
    super.initState();
    _fetchRestaurants();
  }

  Future<void> _fetchRestaurants({bool refresh = false}) async {
    if (_isLoading) return;
    if (refresh) {
      _page = 0;
      _restaurants.clear();
      _hasMore = true;
    }

    setState(() => _isLoading = true);

    try {
      // Get device location
      Position pos = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      final result = await _api.getNearbyRestaurants(
        latitude: pos.latitude,
        longitude: pos.longitude,
        radiusKm: 10,
        page: _page,
        sortBy: 'distance',
      );

      setState(() {
        _restaurants.addAll(result.content);
        _hasMore = !result.last;
        _page++;
      });
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Restaurants Near You')),
      body: RefreshIndicator(
        onRefresh: () => _fetchRestaurants(refresh: true),
        child: ListView.builder(
          itemCount: _restaurants.length + (_hasMore ? 1 : 0),
          itemBuilder: (context, index) {
            if (index == _restaurants.length) {
              // Load more trigger
              _fetchRestaurants();
              return const Center(child: CircularProgressIndicator());
            }

            final r = _restaurants[index];
            return ListTile(
              leading: r.logoUrl != null
                  ? Image.network(r.logoUrl!, width: 50, height: 50, fit: BoxFit.cover)
                  : const Icon(Icons.restaurant),
              title: Text(r.restaurantName),
              subtitle: Text(
                '${r.cuisineType} • '
                '${r.distanceDisplay ?? ''} • '
                '⭐ ${r.rating.toStringAsFixed(1)}',
              ),
              trailing: r.estimatedDeliveryMinutes != null
                  ? Text('~${r.estimatedDeliveryMinutes} min')
                  : null,
            );
          },
        ),
      ),
    );
  }
}
```

---

## Validation Rules Summary

| Parameter | Rule | Error Message |
|-----------|------|---------------|
| `latitude` | Must be in `[-90, 90]` | `"Latitude must be between -90 and 90"` |
| `longitude` | Must be in `[-180, 180]` | `"Longitude must be between -180 and 180"` |
| `radiusKm` | Must be in `(0, 50]` | `"Radius must be between 0 and 50 km"` |

---

## Notes for Integration Teams

1. **GPS permissions** — Both web (`navigator.geolocation`) and mobile (Geolocator plugin) require explicit user permission for location access before calling this endpoint.

2. **Distance fields are optional** — `distanceFromUser`, `distanceDisplay`, and `estimatedDeliveryMinutes` are only included when the restaurant has stored GPS coordinates. Display defensively (null-check before rendering).

3. **Pagination is 0-indexed** — The first page is `page=0`. When `last: true` in the response, there are no more pages to load.

4. **Sort by distance is default** — The backend GeoLocationService pre-sorts by distance, so requesting `sortBy=distance` requires no extra processing.

5. **Delivery fee logic** — If `deliveryType == "SELF_DELIVERY"`, use `baseDeliveryFee + (distanceFromUser * perKmFee)` to calculate the actual delivery fee shown to the customer. If `deliveryType == "SYSTEM_DELIVERY"`, use the flat `deliveryFee` field.

6. **Base URL configuration** — Store `baseUrl` in an environment variable / `flutter_dotenv` values so it can switch between `dev`, `staging`, and `production` environments without code changes.
