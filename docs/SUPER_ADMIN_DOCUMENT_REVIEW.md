# Super Admin Document Review Guide

## Overview
Super Admin can view and download all business documents submitted by restaurant admins before approving or rejecting their restaurant applications.

---

## API Endpoints

### **1. Get All Pending Restaurants with Documents**

**Endpoint:** `GET /api/restaurants/pending`

**Authorization:** `SUPER_ADMIN` role required

**Response:**
```json
[
  {
    "restaurantId": 1,
    "restaurantName": "Pizza Palace",
    "location": "Downtown, City Center",
    "cuisineType": "Italian",
    "email": "info@pizzapalace.com",
    "phoneNumber": "+250782242930",
    "logoUrl": "/api/files/restaurants/temp/logo/abc123.jpg",
    "description": "Authentic Italian pizza and pasta",
    "deliveryFee": 5.0,
    "minimumOrderAmount": 15.0,
    "averagePreparationTime": 30,
    "commercialRegistrationCertificateUrl": "/api/files/restaurants/temp/documents/commercial-registration/xyz789.pdf",
    "taxIdentificationNumber": "123456789",
    "taxIdentificationDocumentUrl": "/api/files/restaurants/temp/documents/tax-identification/def456.pdf",
    "businessOperatingLicenseUrl": "/api/files/restaurants/temp/documents/operating-license/ghi789.pdf",
    "isApproved": false,
    "approvalStatus": "PENDING",
    "rejectionReason": null,
    "reviewedBy": null,
    "reviewedAt": null,
    "createdAt": "2025-10-29",
    "updatedAt": "2025-10-29"
  }
]
```

**Document URLs Included:**
- ✅ `commercialRegistrationCertificateUrl` - Commercial Registration PDF
- ✅ `taxIdentificationDocumentUrl` - NUIT Certificate PDF
- ✅ `businessOperatingLicenseUrl` - Operating License PDF
- ✅ `logoUrl` - Restaurant logo image

---

### **2. Get Specific Restaurant Details for Review**

**Endpoint:** `GET /api/restaurants/{restaurantId}/review-details`

**Authorization:** `SUPER_ADMIN` role required

**Path Parameters:**
- `restaurantId` - ID of the restaurant to review

**Example:**
```
GET /api/restaurants/1/review-details
```

**Response:**
```json
{
  "restaurantId": 1,
  "restaurantName": "Pizza Palace",
  "location": "Downtown, City Center",
  "cuisineType": "Italian",
  "email": "info@pizzapalace.com",
  "phoneNumber": "+250782242930",
  "logoUrl": "/api/files/restaurants/temp/logo/abc123.jpg",
  "description": "Authentic Italian pizza and pasta",
  "deliveryFee": 5.0,
  "minimumOrderAmount": 15.0,
  "averagePreparationTime": 30,
  "commercialRegistrationCertificateUrl": "/api/files/restaurants/temp/documents/commercial-registration/xyz789.pdf",
  "taxIdentificationNumber": "123456789",
  "taxIdentificationDocumentUrl": "/api/files/restaurants/temp/documents/tax-identification/def456.pdf",
  "businessOperatingLicenseUrl": "/api/files/restaurants/temp/documents/operating-license/ghi789.pdf",
  "isApproved": false,
  "approvalStatus": "PENDING",
  "rejectionReason": null,
  "reviewedBy": null,
  "reviewedAt": null,
  "createdAt": "2025-10-29",
  "updatedAt": "2025-10-29"
}
```

---

### **3. Download/View Document**

**Endpoint:** `GET /api/files/{filePath}`

**Authorization:** Required (any authenticated user can view files they have access to)

**Example:**
```
GET /api/files/restaurants/temp/documents/commercial-registration/xyz789.pdf
```

**Response:** PDF file download or inline view

**Note:** The file endpoint should already exist in your `FileController`. If not, you need to implement it.

---

## Document Types

### **1. Commercial Registration Certificate**
- **Field:** `commercialRegistrationCertificateUrl`
- **Format:** PDF
- **Purpose:** Verify business is legally registered
- **What to Check:**
  - Valid registration number
  - Business name matches application
  - Not expired
  - Issued by legitimate authority

### **2. Tax Identification Document (NUIT)**
- **Field:** `taxIdentificationDocumentUrl`
- **Text Field:** `taxIdentificationNumber`
- **Format:** PDF + Text
- **Purpose:** Verify tax compliance
- **What to Check:**
  - NUIT number matches text field
  - Document is official
  - Not expired
  - Business name matches

### **3. Business Operating License**
- **Field:** `businessOperatingLicenseUrl`
- **Format:** PDF
- **Purpose:** Verify permission to operate food business
- **What to Check:**
  - Valid license for food service
  - Location matches application
  - Not expired
  - Proper health/safety certifications

---

## Frontend Integration (Flutter)

### **Get Pending Restaurants with Documents**

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;

class RestaurantReviewModel {
  final int restaurantId;
  final String restaurantName;
  final String location;
  final String email;
  final String? commercialRegistrationCertificateUrl;
  final String? taxIdentificationNumber;
  final String? taxIdentificationDocumentUrl;
  final String? businessOperatingLicenseUrl;
  final String? logoUrl;
  final String approvalStatus;
  final bool isApproved;
  
  RestaurantReviewModel({
    required this.restaurantId,
    required this.restaurantName,
    required this.location,
    required this.email,
    this.commercialRegistrationCertificateUrl,
    this.taxIdentificationNumber,
    this.taxIdentificationDocumentUrl,
    this.businessOperatingLicenseUrl,
    this.logoUrl,
    required this.approvalStatus,
    required this.isApproved,
  });
  
  factory RestaurantReviewModel.fromJson(Map<String, dynamic> json) {
    return RestaurantReviewModel(
      restaurantId: json['restaurantId'],
      restaurantName: json['restaurantName'],
      location: json['location'],
      email: json['email'],
      commercialRegistrationCertificateUrl: json['commercialRegistrationCertificateUrl'],
      taxIdentificationNumber: json['taxIdentificationNumber'],
      taxIdentificationDocumentUrl: json['taxIdentificationDocumentUrl'],
      businessOperatingLicenseUrl: json['businessOperatingLicenseUrl'],
      logoUrl: json['logoUrl'],
      approvalStatus: json['approvalStatus'],
      isApproved: json['isApproved'],
    );
  }
  
  bool hasAllDocuments() {
    return commercialRegistrationCertificateUrl != null &&
           taxIdentificationDocumentUrl != null &&
           businessOperatingLicenseUrl != null &&
           taxIdentificationNumber != null;
  }
  
  int getUploadedDocumentsCount() {
    int count = 0;
    if (commercialRegistrationCertificateUrl != null) count++;
    if (taxIdentificationDocumentUrl != null) count++;
    if (businessOperatingLicenseUrl != null) count++;
    return count;
  }
}

Future<List<RestaurantReviewModel>> getPendingRestaurants(String token) async {
  final response = await http.get(
    Uri.parse('$baseUrl/api/restaurants/pending'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );

  if (response.statusCode == 200) {
    List<dynamic> data = jsonDecode(response.body);
    return data.map((json) => RestaurantReviewModel.fromJson(json)).toList();
  } else {
    throw Exception('Failed to load pending restaurants');
  }
}

Future<RestaurantReviewModel> getRestaurantForReview(int restaurantId, String token) async {
  final response = await http.get(
    Uri.parse('$baseUrl/api/restaurants/$restaurantId/review-details'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );

  if (response.statusCode == 200) {
    return RestaurantReviewModel.fromJson(jsonDecode(response.body));
  } else {
    throw Exception('Failed to load restaurant details');
  }
}
```

### **UI Example - Restaurant Review Screen**

```dart
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

class RestaurantReviewScreen extends StatefulWidget {
  final int restaurantId;
  
  @override
  _RestaurantReviewScreenState createState() => _RestaurantReviewScreenState();
}

class _RestaurantReviewScreenState extends State<RestaurantReviewScreen> {
  RestaurantReviewModel? restaurant;
  bool isLoading = true;
  final _rejectionReasonController = TextEditingController();
  
  @override
  void initState() {
    super.initState();
    _loadRestaurantDetails();
  }
  
  Future<void> _loadRestaurantDetails() async {
    try {
      final data = await getRestaurantForReview(widget.restaurantId, 'YOUR_TOKEN');
      setState(() {
        restaurant = data;
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e')),
      );
    }
  }
  
  Future<void> _openDocument(String? url) async {
    if (url == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Document not uploaded')),
      );
      return;
    }
    
    final fullUrl = '$baseUrl$url';
    if (await canLaunch(fullUrl)) {
      await launch(fullUrl);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Cannot open document')),
      );
    }
  }
  
  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return Scaffold(
        appBar: AppBar(title: Text('Review Restaurant')),
        body: Center(child: CircularProgressIndicator()),
      );
    }
    
    if (restaurant == null) {
      return Scaffold(
        appBar: AppBar(title: Text('Review Restaurant')),
        body: Center(child: Text('Restaurant not found')),
      );
    }
    
    return Scaffold(
      appBar: AppBar(
        title: Text('Review: ${restaurant!.restaurantName}'),
        actions: [
          IconButton(
            icon: Icon(Icons.refresh),
            onPressed: _loadRestaurantDetails,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Restaurant Logo
            if (restaurant!.logoUrl != null)
              Center(
                child: GestureDetector(
                  onTap: () => _openDocument(restaurant!.logoUrl),
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(12),
                    child: Image.network(
                      '$baseUrl${restaurant!.logoUrl}',
                      height: 150,
                      width: 150,
                      fit: BoxFit.cover,
                      errorBuilder: (context, error, stackTrace) {
                        return Container(
                          height: 150,
                          width: 150,
                          color: Colors.grey[300],
                          child: Icon(Icons.restaurant, size: 50),
                        );
                      },
                    ),
                  ),
                ),
              ),
            
            SizedBox(height: 20),
            
            // Restaurant Details
            _buildSectionTitle('Restaurant Information'),
            _buildInfoCard('Name', restaurant!.restaurantName),
            _buildInfoCard('Location', restaurant!.location),
            _buildInfoCard('Email', restaurant!.email),
            _buildInfoCard('NUIT', restaurant!.taxIdentificationNumber ?? 'Not provided'),
            
            SizedBox(height: 20),
            
            // Documents Section
            _buildSectionTitle('Business Documents'),
            _buildDocumentStatus(),
            
            SizedBox(height: 10),
            
            _buildDocumentCard(
              'Commercial Registration Certificate',
              restaurant!.commercialRegistrationCertificateUrl,
              Icons.business,
            ),
            
            _buildDocumentCard(
              'Tax Identification Document (NUIT)',
              restaurant!.taxIdentificationDocumentUrl,
              Icons.receipt_long,
            ),
            
            _buildDocumentCard(
              'Business Operating License',
              restaurant!.businessOperatingLicenseUrl,
              Icons.verified,
            ),
            
            SizedBox(height: 30),
            
            // Rejection Reason Input
            _buildSectionTitle('Review Decision'),
            TextField(
              controller: _rejectionReasonController,
              decoration: InputDecoration(
                labelText: 'Rejection Reason (if rejecting)',
                border: OutlineInputBorder(),
                hintText: 'Provide a clear reason for rejection',
              ),
              maxLines: 3,
            ),
            
            SizedBox(height: 20),
            
            // Action Buttons
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => _approveRestaurant(),
                    icon: Icon(Icons.check_circle),
                    label: Text('Approve'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.green,
                      padding: EdgeInsets.symmetric(vertical: 15),
                    ),
                  ),
                ),
                SizedBox(width: 10),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => _rejectRestaurant(),
                    icon: Icon(Icons.cancel),
                    label: Text('Reject'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      padding: EdgeInsets.symmetric(vertical: 15),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
  
  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: EdgeInsets.only(bottom: 10),
      child: Text(
        title,
        style: TextStyle(
          fontSize: 20,
          fontWeight: FontWeight.bold,
          color: Colors.blue[800],
        ),
      ),
    );
  }
  
  Widget _buildInfoCard(String label, String value) {
    return Card(
      margin: EdgeInsets.only(bottom: 8),
      child: Padding(
        padding: EdgeInsets.all(12),
        child: Row(
          children: [
            Text(
              '$label: ',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            Expanded(child: Text(value)),
          ],
        ),
      ),
    );
  }
  
  Widget _buildDocumentStatus() {
    final hasAll = restaurant!.hasAllDocuments();
    final count = restaurant!.getUploadedDocumentsCount();
    
    return Card(
      color: hasAll ? Colors.green[50] : Colors.orange[50],
      child: Padding(
        padding: EdgeInsets.all(12),
        child: Row(
          children: [
            Icon(
              hasAll ? Icons.check_circle : Icons.warning,
              color: hasAll ? Colors.green : Colors.orange,
            ),
            SizedBox(width: 10),
            Text(
              hasAll 
                ? 'All documents uploaded ✓' 
                : 'Documents uploaded: $count/3',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: hasAll ? Colors.green[800] : Colors.orange[800],
              ),
            ),
          ],
        ),
      ),
    );
  }
  
  Widget _buildDocumentCard(String title, String? url, IconData icon) {
    final isUploaded = url != null;
    
    return Card(
      margin: EdgeInsets.only(bottom: 10),
      child: ListTile(
        leading: Icon(
          icon,
          color: isUploaded ? Colors.blue : Colors.grey,
          size: 30,
        ),
        title: Text(title),
        subtitle: Text(
          isUploaded ? 'Tap to view document' : 'Not uploaded',
          style: TextStyle(
            color: isUploaded ? Colors.green : Colors.red,
          ),
        ),
        trailing: Icon(
          isUploaded ? Icons.visibility : Icons.close,
          color: isUploaded ? Colors.blue : Colors.red,
        ),
        onTap: isUploaded ? () => _openDocument(url) : null,
      ),
    );
  }
  
  Future<void> _approveRestaurant() async {
    // Call approval API
    // ... implementation
  }
  
  Future<void> _rejectRestaurant() async {
    if (_rejectionReasonController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Please provide a rejection reason')),
      );
      return;
    }
    // Call rejection API
    // ... implementation
  }
}
```

---

## Testing with Postman

### **Test 1: Get Pending Restaurants**

**Request:**
```
GET http://localhost:8085/api/restaurants/pending
Authorization: Bearer SUPER_ADMIN_TOKEN
```

**Expected Response:** List of restaurants with all document URLs

### **Test 2: Get Specific Restaurant Details**

**Request:**
```
GET http://localhost:8085/api/restaurants/1/review-details
Authorization: Bearer SUPER_ADMIN_TOKEN
```

**Expected Response:** Full restaurant details with all document URLs

### **Test 3: View/Download Document**

**Request:**
```
GET http://localhost:8085/api/files/restaurants/temp/documents/commercial-registration/xyz789.pdf
Authorization: Bearer SUPER_ADMIN_TOKEN
```

**Expected Response:** PDF file download or inline view

---

## Review Checklist for Super Admin

### **Before Approving:**

✅ **Commercial Registration Certificate:**
- [ ] Document is clear and readable
- [ ] Business name matches application
- [ ] Registration number is valid
- [ ] Document is not expired
- [ ] Issued by legitimate authority

✅ **Tax Identification (NUIT):**
- [ ] NUIT number matches text field
- [ ] Document is official government-issued
- [ ] Business name matches
- [ ] Document is not expired

✅ **Business Operating License:**
- [ ] Valid for food service operations
- [ ] Location matches application
- [ ] License is current and not expired
- [ ] Includes required health/safety certifications

✅ **General:**
- [ ] All 3 documents are uploaded
- [ ] Restaurant information is complete
- [ ] Contact information is valid
- [ ] No red flags or inconsistencies

---

## Summary

✅ **View Documents** - All document URLs included in API response  
✅ **Download Documents** - Click URLs to view/download PDFs  
✅ **Document Status** - Helper methods show upload completion  
✅ **Detailed Review** - Dedicated endpoint for single restaurant  
✅ **Type Safety** - RestaurantReviewDTO for structured data  

Super Admin can now fully review all documents before making approval decisions! 🎉
