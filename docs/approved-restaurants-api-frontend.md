# Approved Restaurants API — Frontend (Web)

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

## Step 1 — Get User Location

The browser's built-in Geolocation API is used. No library needed.

```typescript
function getUserLocation(): Promise<{ latitude: number; longitude: number }> {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      return reject(new Error('Geolocation not supported by this browser.'));
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => resolve({ latitude: pos.coords.latitude, longitude: pos.coords.longitude }),
      (err) => reject(new Error(err.message)),
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
    );
  });
}
```

> The browser will show a **"Allow location?"** permission dialog on first use. Coordinates are only returned if the user clicks **Allow**.

---

## Step 2 — Call the API

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

  if (options?.cuisineType)    params.append('cuisineType', options.cuisineType);
  if (options?.minRating != null)      params.append('minRating', String(options.minRating));
  if (options?.maxDeliveryFee != null) params.append('maxDeliveryFee', String(options.maxDeliveryFee));

  const res = await fetch(`/api/restaurants/approved?${params}`);
  if (!res.ok) throw new Error((await res.json()).error ?? 'Request failed');
  return res.json(); // Page<RestaurantDTO>
}
```

---

## Step 3 — Put it Together

```typescript
async function loadRestaurants() {
  try {
    const { latitude, longitude } = await getUserLocation();
    const page = await getNearbyRestaurants(latitude, longitude, {
      radiusKm: 5,
      sortBy: 'rating',
    });

    console.log(`${page.totalElements} restaurants found`);
    page.content.forEach((r) =>
      console.log(`${r.restaurantName} · ${r.distanceDisplay} · ⭐ ${r.rating}`)
    );
  } catch (err) {
    console.error('Error:', err.message);
    // Show fallback UI or manual location input
  }
}
```

---

## Response Shape (`Page<RestaurantDTO>`)

```json
{
  "content": [
    {
      "restaurantId": 1,
      "restaurantName": "Café Maputo",
      "cuisineType": "African",
      "rating": 4.5,
      "totalReviews": 120,
      "deliveryFee": 50.0,
      "deliveryType": "SYSTEM_DELIVERY",
      "minimumOrderAmount": 200.0,
      "distanceFromUser": 1.3,
      "distanceDisplay": "1.3 km",
      "estimatedDeliveryMinutes": 18,
      "logoUrl": "/api/files/restaurants/1/logo.png",
      "isActive": true
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

### Key fields

| Field | Notes |
|-------|-------|
| `distanceFromUser` | km from customer. May be `null` — check before displaying. |
| `distanceDisplay` | Ready-to-display string e.g. `"1.3 km"`. |
| `estimatedDeliveryMinutes` | Estimated ETA. May be `null`. |
| `deliveryType` | `SYSTEM_DELIVERY` → use `deliveryFee`. `SELF_DELIVERY` → use `baseDeliveryFee + distanceFromUser * perKmFee`. |

---

## Error Responses (400)

```json
{ "error": "Latitude must be between -90 and 90" }
{ "error": "Longitude must be between -180 and 180" }
{ "error": "Radius must be between 0 and 50 km" }
```

---

## Pagination (Load More)

```typescript
let page = 0;

async function loadMore(lat: number, lng: number) {
  const data = await getNearbyRestaurants(lat, lng, { page, size: 20 });
  renderList(data.content);       // append to UI
  if (!data.last) page++;         // stop incrementing once last page reached
}
```
