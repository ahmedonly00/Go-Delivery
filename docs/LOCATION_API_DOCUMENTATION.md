# Location Management API Documentation

## Overview
This document describes the RESTful API endpoints for managing countries, cities, and customer delivery addresses in the Go Delivery application.

## Base URL
```
http://localhost:8085/api/locations
```

---

## Endpoints

### 1. Get All Countries
Retrieves a list of all available countries.

**Endpoint:** `GET /api/locations/countries`

**Response:**
```json
{
  "success": true,
  "message": "Countries retrieved successfully",
  "data": [
    {
      "countryId": 1,
      "countryName": "United States",
      "countryCode": "USA"
    }
  ]
}
```

---

### 2. Get Cities by Country
Retrieves all cities within a specific country.

**Endpoint:** `GET /api/locations/countries/{countryId}/cities`

**Path Parameters:**
- `countryId` (Long) - The ID of the country

**Response:**
```json
{
  "success": true,
  "message": "Cities retrieved successfully",
  "data": [
    {
      "cityId": 1,
      "cityName": "New York",
      "countryId": 1
    }
  ]
}
```

**Error Response (Country Not Found):**
```json
{
  "success": false,
  "message": "Country not found with ID: 999"
}
```

---

### 3. Create Address
Creates a new delivery address for a customer with optional image upload.

**Endpoint:** `POST /api/locations/addresses`

**Content-Type:** `multipart/form-data`

**Form Parameters:**
- `customerId` (Long, required) - The customer's ID
- `cityId` (Long, required) - The city ID
- `street` (String, required) - Street name
- `areaName` (String, required) - Area/neighborhood name
- `houseNumber` (String, required) - House/building number
- `localContactNumber` (String, required) - Local contact phone number
- `latitude` (Float, required) - GPS latitude coordinate
- `longitude` (Float, required) - GPS longitude coordinate
- `addressType` (String, required) - Address type: `HOME`, `WORK`, or `OTHER`
- `usageOption` (String, required) - Usage option: `Permanent` or `Temporary`
- `isDefault` (Boolean, optional, default: false) - Set as default address
- `image` (File, optional) - Location image (jpg, jpeg, png, gif)

**Response:**
```json
{
  "success": true,
  "message": "Address created successfully",
  "data": {
    "customerAddressId": 1,
    "customerId": 1,
    "cityId": 1,
    "cityName": "New York",
    "street": "5th Avenue",
    "areaName": "Manhattan",
    "houseNumber": "123",
    "localContactNumber": "+1234567890",
    "latitude": 40.7589,
    "longitude": -73.9851,
    "addressType": "HOME",
    "usageOption": "Permanent",
    "imageUrl": "/uploads/addresses/uuid-filename.jpg",
    "isDefault": true,
    "createdAt": "2024-10-24",
    "updatedAt": "2024-10-24"
  }
}
```

**Error Response (Customer Not Found):**
```json
{
  "success": false,
  "message": "Customer not found with ID: 999"
}
```

**Error Response (City Not Found):**
```json
{
  "success": false,
  "message": "City not found with ID: 999"
}
```

---

### 4. Get Customer Addresses
Retrieves all addresses for a specific customer, ordered by default status.

**Endpoint:** `GET /api/locations/addresses?customerId={customerId}`

**Query Parameters:**
- `customerId` (Long, required) - The customer's ID

**Response:**
```json
{
  "success": true,
  "message": "Addresses retrieved successfully",
  "data": [
    {
      "customerAddressId": 1,
      "customerId": 1,
      "cityId": 1,
      "cityName": "New York",
      "street": "5th Avenue",
      "areaName": "Manhattan",
      "houseNumber": "123",
      "localContactNumber": "+1234567890",
      "latitude": 40.7589,
      "longitude": -73.9851,
      "addressType": "HOME",
      "usageOption": "Permanent",
      "imageUrl": "/uploads/addresses/uuid-filename.jpg",
      "isDefault": true,
      "createdAt": "2024-10-24",
      "updatedAt": "2024-10-24"
    }
  ]
}
```

---

### 5. Get Address by ID
Retrieves a specific address by its ID.

**Endpoint:** `GET /api/locations/addresses/{addressId}`

**Path Parameters:**
- `addressId` (Long) - The address ID

**Response:**
```json
{
  "success": true,
  "message": "Address retrieved successfully",
  "data": {
    "customerAddressId": 1,
    "customerId": 1,
    "cityId": 1,
    "cityName": "New York",
    "street": "5th Avenue",
    "areaName": "Manhattan",
    "houseNumber": "123",
    "localContactNumber": "+1234567890",
    "latitude": 40.7589,
    "longitude": -73.9851,
    "addressType": "HOME",
    "usageOption": "Permanent",
    "imageUrl": "/uploads/addresses/uuid-filename.jpg",
    "isDefault": true,
    "createdAt": "2024-10-24",
    "updatedAt": "2024-10-24"
  }
}
```

**Error Response (Address Not Found):**
```json
{
  "success": false,
  "message": "Address not found with ID: 999"
}
```

---

### 6. Delete Address
Deletes a specific address.

**Endpoint:** `DELETE /api/locations/addresses/{addressId}`

**Path Parameters:**
- `addressId` (Long) - The address ID

**Response:**
```json
{
  "success": true,
  "message": "Address deleted successfully"
}
```

**Error Response (Address Not Found):**
```json
{
  "success": false,
  "message": "Address not found with ID: 999"
}
```

---

## Data Models

### Country
```java
{
  "countryId": Long,
  "countryName": String,
  "countryCode": String
}
```

### City
```java
{
  "cityId": Long,
  "cityName": String,
  "countryId": Long
}
```

### Address
```java
{
  "customerAddressId": Long,
  "customerId": Long,
  "cityId": Long,
  "cityName": String,
  "street": String,
  "areaName": String,
  "houseNumber": String,
  "localContactNumber": String,
  "latitude": Float,
  "longitude": Float,
  "addressType": "HOME" | "WORK" | "OTHER",
  "usageOption": String,
  "imageUrl": String,
  "isDefault": Boolean,
  "createdAt": LocalDate,
  "updatedAt": LocalDate
}
```

---

## File Upload

### Supported Image Formats
- JPG/JPEG
- PNG
- GIF

### Maximum File Size
5MB (configured in `application.properties`)

### Storage Location
Images are stored in the `uploads/addresses/` directory and served via `/uploads/addresses/{filename}`

### Image URL Format
```
http://localhost:8085/uploads/addresses/uuid-filename.jpg
```

---

## Error Handling

All endpoints return consistent error responses:

```json
{
  "success": false,
  "message": "Error description"
}
```

### HTTP Status Codes
- `200 OK` - Successful GET/DELETE request
- `201 Created` - Successful POST request
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Database Schema

### Tables Created

#### country
- `country_id` (BIGINT, PK)
- `country_name` (VARCHAR, UNIQUE)
- `country_code` (VARCHAR(3), UNIQUE)
- `created_at` (DATE)
- `updated_at` (DATE)

#### city
- `city_id` (BIGINT, PK)
- `city_name` (VARCHAR)
- `country_id` (BIGINT, FK → country)
- `created_at` (DATE)
- `updated_at` (DATE)

#### customer_address
- `address_id` (BIGINT, PK)
- `customer_id` (BIGINT, FK → customer)
- `city_id` (BIGINT, FK → city)
- `street` (VARCHAR)
- `area_name` (VARCHAR)
- `house_number` (VARCHAR)
- `local_contact_number` (VARCHAR)
- `latitude` (FLOAT)
- `longitude` (FLOAT)
- `address_type` (VARCHAR - ENUM)
- `usage_option` (VARCHAR)
- `image_url` (VARCHAR)
- `is_default` (BOOLEAN)
- `created_at` (DATE)
- `updated_at` (DATE)

---

## Testing with Postman/cURL

### Example: Create Address with Image

**cURL:**
```bash
curl -X POST http://localhost:8085/api/locations/addresses \
  -F "customerId=1" \
  -F "cityId=1" \
  -F "street=5th Avenue" \
  -F "areaName=Manhattan" \
  -F "houseNumber=123" \
  -F "localContactNumber=+1234567890" \
  -F "latitude=40.7589" \
  -F "longitude=-73.9851" \
  -F "addressType=HOME" \
  -F "usageOption=Permanent" \
  -F "isDefault=true" \
  -F "image=@/path/to/image.jpg"
```

**Postman:**
1. Set method to POST
2. URL: `http://localhost:8085/api/locations/addresses`
3. Body → form-data
4. Add all required fields as text
5. Add `image` field as File type and select image

---

## Notes

1. **Default Address Logic**: When setting an address as default (`isDefault=true`), all other addresses for that customer are automatically set to non-default.

2. **Image Upload**: The image parameter is optional. If not provided, the address will be created without an image.

3. **Address Type**: Must be one of: `HOME`, `WORK`, or `OTHER` (case-insensitive in the API, converted to uppercase).

4. **Usage Option**: Typically `Permanent` or `Temporary`, but accepts any string value.

5. **GPS Coordinates**: Latitude and longitude should be obtained from Google Maps or the device's location services.

6. **File Storage**: Uploaded images are stored locally in the `uploads/addresses/` directory with UUID-based filenames to prevent conflicts.

7. **CORS**: The controller is configured with `@CrossOrigin(origins = "*")` for development. Update this for production.

---

## Integration with Flutter

### Example Flutter Code

```dart
// Create address with image
Future<void> createAddress({
  required int customerId,
  required int cityId,
  required String street,
  required String areaName,
  required String houseNumber,
  required String localContactNumber,
  required double latitude,
  required double longitude,
  required String addressType,
  required String usageOption,
  bool isDefault = false,
  File? image,
}) async {
  var request = http.MultipartRequest(
    'POST',
    Uri.parse('http://localhost:8085/api/locations/addresses'),
  );

  request.fields['customerId'] = customerId.toString();
  request.fields['cityId'] = cityId.toString();
  request.fields['street'] = street;
  request.fields['areaName'] = areaName;
  request.fields['houseNumber'] = houseNumber;
  request.fields['localContactNumber'] = localContactNumber;
  request.fields['latitude'] = latitude.toString();
  request.fields['longitude'] = longitude.toString();
  request.fields['addressType'] = addressType;
  request.fields['usageOption'] = usageOption;
  request.fields['isDefault'] = isDefault.toString();

  if (image != null) {
    request.files.add(
      await http.MultipartFile.fromPath('image', image.path),
    );
  }

  var response = await request.send();
  var responseData = await response.stream.bytesToString();
  
  if (response.statusCode == 201) {
    print('Address created successfully');
  } else {
    print('Error: $responseData');
  }
}
```

---

## Setup Instructions

### 1. Database Setup
The tables will be created automatically by Hibernate when you start the application (due to `spring.jpa.hibernate.ddl-auto=update`).

### 2. Seed Data (Optional)
You may want to seed some countries and cities. Create a data initializer:

```java
@Component
public class LocationDataInitializer implements CommandLineRunner {
    
    @Autowired
    private CountryRepository countryRepository;
    
    @Autowired
    private CityRepository cityRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (countryRepository.count() == 0) {
            // Create countries
            Country usa = Country.builder()
                .countryName("United States")
                .countryCode("USA")
                .build();
            countryRepository.save(usa);
            
            // Create cities
            City newYork = City.builder()
                .cityName("New York")
                .country(usa)
                .build();
            cityRepository.save(newYork);
        }
    }
}
```

### 3. Create Uploads Directory
The directory will be created automatically, but you can create it manually:
```bash
mkdir uploads
mkdir uploads/addresses
```

---

## Troubleshooting

### Issue: Images not loading
- Check that the `uploads/` directory exists
- Verify `WebMvcConfig` is serving static resources
- Check file permissions on the uploads directory

### Issue: File upload fails
- Verify `spring.servlet.multipart.max-file-size` in `application.properties`
- Check that the file format is supported
- Ensure the file size is under 5MB

### Issue: Country/City not found
- Make sure you have seeded the database with countries and cities
- Verify the IDs being sent from Flutter match the database

---

## Future Enhancements

1. **Update Address**: Add PUT endpoint to update existing addresses
2. **Set Default Address**: Add dedicated endpoint to change default address
3. **Address Validation**: Add validation for duplicate addresses
4. **Image Compression**: Compress uploaded images to save storage
5. **Geocoding**: Add reverse geocoding to auto-fill address details from coordinates
6. **Address Search**: Add search/filter capabilities for addresses
7. **Pagination**: Add pagination for large address lists
8. **Soft Delete**: Implement soft delete instead of hard delete
