# Approved Restaurants API — Flutter (Mobile)

## Endpoint

```
GET /api/restaurants/approved
```

No authentication required.

---

## Parameters

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `latitude` | ✅ | — | Customer latitude (`-90` to `90`) |
| `longitude` | ✅ | — | Customer longitude (`-180` to `180`) |
| `radiusKm` | No | `10.0` | Search radius, max `50` km |
| `page` | No | `0` | Page number (0-indexed) |
| `size` | No | `20` | Results per page |
| `sortBy` | No | `distance` | `distance` \| `rating` \| `popularity` |
| `cuisineType` | No | — | e.g. `African`, `Italian` (case-insensitive) |
| `minRating` | No | — | Minimum restaurant rating |
| `maxDeliveryFee` | No | — | Maximum delivery fee |

---

## Step 1 — Setup: GPS Permission

### `pubspec.yaml`
```yaml
dependencies:
  geolocator: ^12.0.0
  http: ^1.2.0
```

### Android — `AndroidManifest.xml`
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

### iOS — `Info.plist`
```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>GoDelivery needs your location to show nearby restaurants.</string>
```

---

## Step 2 — Location Service

```dart
import 'package:geolocator/geolocator.dart';

class LocationService {
  static Future<Position> getCurrentPosition() async {
    if (!await Geolocator.isLocationServiceEnabled()) {
      throw Exception('GPS is disabled. Please turn on location services.');
    }

    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) {
        throw Exception('Location permission denied.');
      }
    }
    if (permission == LocationPermission.deniedForever) {
      throw Exception('Location permission permanently denied. Enable it in Settings.');
    }

    return Geolocator.getCurrentPosition(
      desiredAccuracy: LocationAccuracy.high,
      timeLimit: const Duration(seconds: 10),
    );
  }
}
```

---

## Step 3 — Model Classes

### `RestaurantDTO`
```dart
class RestaurantDTO {
  final int restaurantId;
  final String restaurantName;
  final String cuisineType;
  final double rating;
  final int totalReviews;
  final String deliveryType;
  final double? deliveryFee;
  final double? minimumOrderAmount;
  final int? averagePreparationTime;
  final double? distanceFromUser;
  final String? distanceDisplay;
  final int? estimatedDeliveryMinutes;
  final String? logoUrl;
  final bool isActive;

  RestaurantDTO.fromJson(Map<String, dynamic> j)
      : restaurantId = j['restaurantId'],
        restaurantName = j['restaurantName'],
        cuisineType = j['cuisineType'],
        rating = (j['rating'] as num?)?.toDouble() ?? 0.0,
        totalReviews = j['totalReviews'] ?? 0,
        deliveryType = j['deliveryType'] ?? 'SYSTEM_DELIVERY',
        deliveryFee = (j['deliveryFee'] as num?)?.toDouble(),
        minimumOrderAmount = (j['minimumOrderAmount'] as num?)?.toDouble(),
        averagePreparationTime = j['averagePreparationTime'],
        distanceFromUser = (j['distanceFromUser'] as num?)?.toDouble(),
        distanceDisplay = j['distanceDisplay'],
        estimatedDeliveryMinutes = j['estimatedDeliveryMinutes'],
        logoUrl = j['logoUrl'],
        isActive = j['isActive'] ?? true;
}
```

### `PagedResponse<T>`
```dart
class PagedResponse<T> {
  final List<T> content;
  final int totalElements;
  final bool last;

  PagedResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJson,
  )   : content = (json['content'] as List)
            .map((e) => fromJson(e as Map<String, dynamic>))
            .toList(),
        totalElements = json['totalElements'] ?? 0,
        last = json['last'] ?? true;
}
```

---

## Step 4 — API Service

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;

class RestaurantApiService {
  final String baseUrl; // e.g. 'https://api.godelivery.mz'

  const RestaurantApiService({required this.baseUrl});

  Future<PagedResponse<RestaurantDTO>> getNearbyRestaurants({
    required double latitude,
    required double longitude,
    double radiusKm = 10.0,
    int page = 0,
    int size = 20,
    String sortBy = 'distance',
    String? cuisineType,
    double? minRating,
    double? maxDeliveryFee,
  }) async {
    final uri = Uri.parse('$baseUrl/api/restaurants/approved').replace(
      queryParameters: {
        'latitude': '$latitude',
        'longitude': '$longitude',
        'radiusKm': '$radiusKm',
        'page': '$page',
        'size': '$size',
        'sortBy': sortBy,
        if (cuisineType != null) 'cuisineType': cuisineType,
        if (minRating != null) 'minRating': '$minRating',
        if (maxDeliveryFee != null) 'maxDeliveryFee': '$maxDeliveryFee',
      },
    );

    final response = await http.get(uri);
    if (response.statusCode == 200) {
      return PagedResponse.fromJson(
        jsonDecode(response.body), RestaurantDTO.fromJson);
    } else {
      final err = jsonDecode(response.body);
      throw Exception(err['error'] ?? 'Failed: ${response.statusCode}');
    }
  }
}
```

---

## Step 5 — Screen with Infinite Scroll

```dart
class NearbyRestaurantsScreen extends StatefulWidget {
  const NearbyRestaurantsScreen({super.key});
  @override
  State<NearbyRestaurantsScreen> createState() => _State();
}

class _State extends State<NearbyRestaurantsScreen> {
  final _api = RestaurantApiService(baseUrl: 'https://api.godelivery.mz');
  final List<RestaurantDTO> _items = [];
  int _page = 0;
  bool _loading = false;
  bool _hasMore = true;

  @override
  void initState() { super.initState(); _load(); }

  Future<void> _load({bool refresh = false}) async {
    if (_loading) return;
    if (refresh) { _page = 0; _items.clear(); _hasMore = true; }
    setState(() => _loading = true);
    try {
      final pos = await LocationService.getCurrentPosition();
      final result = await _api.getNearbyRestaurants(
        latitude: pos.latitude,
        longitude: pos.longitude,
        page: _page,
      );
      setState(() {
        _items.addAll(result.content);
        _hasMore = !result.last;
        _page++;
      });
    } catch (e) {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text('$e')));
    } finally {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Nearby Restaurants')),
      body: RefreshIndicator(
        onRefresh: () => _load(refresh: true),
        child: ListView.builder(
          itemCount: _items.length + (_hasMore ? 1 : 0),
          itemBuilder: (ctx, i) {
            if (i == _items.length) {
              _load();
              return const Center(child: CircularProgressIndicator());
            }
            final r = _items[i];
            return ListTile(
              leading: r.logoUrl != null
                  ? Image.network(r.logoUrl!, width: 48, height: 48, fit: BoxFit.cover)
                  : const Icon(Icons.restaurant),
              title: Text(r.restaurantName),
              subtitle: Text(
                '${r.cuisineType} · ${r.distanceDisplay ?? ''} · ⭐ ${r.rating.toStringAsFixed(1)}'),
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

## Response Key Fields

| Field | Notes |
|-------|-------|
| `distanceFromUser` | km from customer. Can be `null` — guard before use. |
| `distanceDisplay` | Ready-to-display string e.g. `"1.3 km"`. |
| `estimatedDeliveryMinutes` | Estimated ETA. Can be `null`. |
| `deliveryType` | `SYSTEM_DELIVERY` → use flat `deliveryFee`. `SELF_DELIVERY` → use `baseDeliveryFee + distanceFromUser * perKmFee`. |

---

## Error Responses (HTTP 400)

```json
{ "error": "Latitude must be between -90 and 90" }
{ "error": "Longitude must be between -180 and 180" }
{ "error": "Radius must be between 0 and 50 km" }
```
