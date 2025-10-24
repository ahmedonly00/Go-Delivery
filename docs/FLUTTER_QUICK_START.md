# Flutter Quick Start - Location API

## API Endpoints

```
GET  /api/locations/countries
GET  /api/locations/countries/{countryId}/cities
POST /api/locations/addresses (multipart/form-data)
GET  /api/locations/addresses?customerId={id}
DELETE /api/locations/addresses/{addressId}
```

## Required Packages

```yaml
dependencies:
  http: ^1.1.0
  image_picker: ^1.0.4
  geolocator: ^10.1.0
```

## Service Class

```dart
class LocationService {
  static const String baseUrl = 'http://localhost:8085/api/locations';
  
  Future<List<Country>> getCountries() async {
    final response = await http.get(Uri.parse('$baseUrl/countries'));
    final data = jsonDecode(response.body);
    return (data['data'] as List).map((json) => Country.fromJson(json)).toList();
  }
  
  Future<Address> createAddress({...}) async {
    var request = http.MultipartRequest('POST', Uri.parse('$baseUrl/addresses'));
    request.fields['customerId'] = customerId.toString();
    // ... add other fields
    if (image != null) {
      request.files.add(await http.MultipartFile.fromPath('image', image.path));
    }
    var response = await request.send();
    return Address.fromJson(jsonDecode(await response.stream.bytesToString())['data']);
  }
}
```

See full documentation in LOCATION_API_DOCUMENTATION.md
