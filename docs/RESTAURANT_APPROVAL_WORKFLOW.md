# Restaurant Approval Workflow

## Overview
After a restaurant admin completes the restaurant setup with all required documents, the Super Admin must review and approve/reject the restaurant before it becomes visible to customers.

---

## Workflow Steps

### **1. Restaurant Registration (Restaurant Admin)**
- Restaurant admin registers and uploads:
  - Basic restaurant information
  - Logo
  - Commercial Registration Certificate (PDF)
  - Tax Identification Number (NUIT) - text + PDF
  - Business Operating License (PDF)
- Status set to: `PENDING`
- `isApproved`: `false`

### **2. Super Admin Reviews Documents**
- Super Admin views pending restaurants
- Reviews all uploaded documents
- Makes decision: Approve or Reject

### **3. Approval Decision**

#### **If Approved:**
- ✅ `isApproved` = `true`
- ✅ `approvalStatus` = `APPROVED`
- ✅ Restaurant becomes visible to customers
- ✅ Email sent to restaurant admin: "Your restaurant is now live!"

#### **If Rejected:**
- ❌ `isApproved` = `false`
- ❌ `approvalStatus` = `REJECTED`
- ❌ `isActive` = `false`
- ❌ Restaurant NOT visible to customers
- ❌ Email sent to restaurant admin with rejection reason

---

## Database Schema

### **New Fields in Restaurant Table**

```sql
ALTER TABLE restaurant 
ADD COLUMN is_approved BOOLEAN DEFAULT FALSE,
ADD COLUMN approval_status VARCHAR(20) DEFAULT 'PENDING',
ADD COLUMN rejection_reason TEXT,
ADD COLUMN reviewed_by VARCHAR(255),
ADD COLUMN reviewed_at DATE;
```

**Field Descriptions:**
- `is_approved`: Boolean flag for quick filtering
- `approval_status`: PENDING, APPROVED, REJECTED
- `rejection_reason`: Reason provided by Super Admin if rejected
- `reviewed_by`: Email of Super Admin who reviewed
- `reviewed_at`: Date when review was completed

---

## API Endpoints

### **1. Get Pending Restaurants (Super Admin)**

**Endpoint:** `GET /api/restaurants/pending`

**Authorization:** `SUPER_ADMIN` role required

**Response:**
```json
[
  {
    "restaurantId": 1,
    "restaurantName": "Pizza Palace",
    "email": "info@pizzapalace.com",
    "location": "Downtown",
    "commercialRegistrationCertificateUrl": "/api/files/restaurants/temp/documents/commercial-registration/uuid.pdf",
    "taxIdentificationNumber": "123456789",
    "taxIdentificationDocumentUrl": "/api/files/restaurants/temp/documents/tax-identification/uuid.pdf",
    "businessOperatingLicenseUrl": "/api/files/restaurants/temp/documents/operating-license/uuid.pdf",
    "approvalStatus": "PENDING",
    "isApproved": false,
    "createdAt": "2025-10-29"
  }
]
```

---

### **2. Get Restaurants by Approval Status (Super Admin)**

**Endpoint:** `GET /api/restaurants/byApprovalStatus/{status}`

**Authorization:** `SUPER_ADMIN` role required

**Path Parameters:**
- `status`: PENDING, APPROVED, or REJECTED

**Example:**
```
GET /api/restaurants/byApprovalStatus/APPROVED
```

---

### **3. Review Restaurant (Super Admin)**

**Endpoint:** `POST /api/restaurants/{restaurantId}/review`

**Authorization:** `SUPER_ADMIN` role required

**Request Body:**
```json
{
  "approved": true,
  "rejectionReason": null
}
```

**For Rejection:**
```json
{
  "approved": false,
  "rejectionReason": "The commercial registration certificate is expired. Please upload a valid document."
}
```

**Success Response (200 OK):**
```json
{
  "message": "Restaurant approved successfully! Notification email has been sent to the restaurant admin.",
  "restaurant": {
    "restaurantId": 1,
    "restaurantName": "Pizza Palace",
    "isApproved": true,
    "approvalStatus": "APPROVED",
    "reviewedBy": "admin@mozfood.com",
    "reviewedAt": "2025-10-29"
  }
}
```

---

### **4. Get Approved Restaurants (Customer)**

**Endpoint:** `GET /api/restaurants/approved`

**Authorization:** None required (public endpoint)

**Response:**
```json
[
  {
    "restaurantId": 1,
    "restaurantName": "Pizza Palace",
    "location": "Downtown",
    "cuisineType": "Italian",
    "rating": 4.5,
    "deliveryFee": 5.0,
    "isApproved": true,
    "approvalStatus": "APPROVED"
  }
]
```

**Note:** Only approved AND active restaurants are returned.

---

## Email Notifications

### **Approval Email**

**Subject:** 🎉 Your Restaurant Has Been Approved!

**Content:**
- Congratulations message
- Restaurant is now live
- Customers can see and order
- Next steps for restaurant management
- Link to dashboard

### **Rejection Email**

**Subject:** Restaurant Application Update - [Restaurant Name]

**Content:**
- Application review update
- Specific rejection reason
- Steps to address issues
- Contact support information
- Option to resubmit

---

## Frontend Integration (Flutter)

### **Super Admin: View Pending Restaurants**

```dart
Future<List<Restaurant>> getPendingRestaurants(String token) async {
  final response = await http.get(
    Uri.parse('$baseUrl/api/restaurants/pending'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );

  if (response.statusCode == 200) {
    List<dynamic> data = jsonDecode(response.body);
    return data.map((json) => Restaurant.fromJson(json)).toList();
  } else {
    throw Exception('Failed to load pending restaurants');
  }
}
```

### **Super Admin: Review Restaurant**

```dart
Future<Map<String, dynamic>> reviewRestaurant({
  required int restaurantId,
  required bool approved,
  String? rejectionReason,
  required String token,
}) async {
  final response = await http.post(
    Uri.parse('$baseUrl/api/restaurants/$restaurantId/review'),
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
    body: jsonEncode({
      'approved': approved,
      'rejectionReason': rejectionReason,
    }),
  );

  if (response.statusCode == 200) {
    return jsonDecode(response.body);
  } else {
    throw Exception('Failed to review restaurant');
  }
}
```

### **Super Admin: Review UI Example**

```dart
class RestaurantReviewScreen extends StatefulWidget {
  final Restaurant restaurant;
  
  @override
  _RestaurantReviewScreenState createState() => _RestaurantReviewScreenState();
}

class _RestaurantReviewScreenState extends State<RestaurantReviewScreen> {
  final _rejectionReasonController = TextEditingController();
  
  Future<void> _approveRestaurant() async {
    try {
      final result = await reviewRestaurant(
        restaurantId: widget.restaurant.id,
        approved: true,
        token: 'YOUR_TOKEN',
      );
      
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(result['message']),
          backgroundColor: Colors.green,
        ),
      );
      
      Navigator.pop(context, true);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
    }
  }
  
  Future<void> _rejectRestaurant() async {
    if (_rejectionReasonController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Please provide a rejection reason')),
      );
      return;
    }
    
    try {
      final result = await reviewRestaurant(
        restaurantId: widget.restaurant.id,
        approved: false,
        rejectionReason: _rejectionReasonController.text,
        token: 'YOUR_TOKEN',
      );
      
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(result['message']),
          backgroundColor: Colors.orange,
        ),
      );
      
      Navigator.pop(context, true);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
    }
  }
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Review Restaurant')),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              widget.restaurant.name,
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 20),
            
            // Restaurant Details
            _buildInfoCard('Email', widget.restaurant.email),
            _buildInfoCard('Location', widget.restaurant.location),
            _buildInfoCard('Cuisine', widget.restaurant.cuisineType),
            _buildInfoCard('NUIT', widget.restaurant.taxIdentificationNumber ?? 'N/A'),
            
            SizedBox(height: 20),
            
            // Documents
            Text('Documents', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            SizedBox(height: 10),
            
            if (widget.restaurant.commercialRegistrationCertificateUrl != null)
              _buildDocumentLink('Commercial Registration', widget.restaurant.commercialRegistrationCertificateUrl!),
            
            if (widget.restaurant.taxIdentificationDocumentUrl != null)
              _buildDocumentLink('Tax ID Document', widget.restaurant.taxIdentificationDocumentUrl!),
            
            if (widget.restaurant.businessOperatingLicenseUrl != null)
              _buildDocumentLink('Operating License', widget.restaurant.businessOperatingLicenseUrl!),
            
            SizedBox(height: 30),
            
            // Rejection Reason Input
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
                  child: ElevatedButton(
                    onPressed: _approveRestaurant,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.green,
                      padding: EdgeInsets.symmetric(vertical: 15),
                    ),
                    child: Text('Approve', style: TextStyle(fontSize: 16)),
                  ),
                ),
                SizedBox(width: 10),
                Expanded(
                  child: ElevatedButton(
                    onPressed: _rejectRestaurant,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red,
                      padding: EdgeInsets.symmetric(vertical: 15),
                    ),
                    child: Text('Reject', style: TextStyle(fontSize: 16)),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
  
  Widget _buildInfoCard(String label, String value) {
    return Card(
      margin: EdgeInsets.only(bottom: 10),
      child: Padding(
        padding: EdgeInsets.all(12),
        child: Row(
          children: [
            Text('$label: ', style: TextStyle(fontWeight: FontWeight.bold)),
            Expanded(child: Text(value)),
          ],
        ),
      ),
    );
  }
  
  Widget _buildDocumentLink(String label, String url) {
    return ListTile(
      leading: Icon(Icons.picture_as_pdf, color: Colors.red),
      title: Text(label),
      trailing: Icon(Icons.open_in_new),
      onTap: () {
        // Open PDF viewer or download
      },
    );
  }
}
```

### **Customer: Get Approved Restaurants**

```dart
Future<List<Restaurant>> getApprovedRestaurants() async {
  final response = await http.get(
    Uri.parse('$baseUrl/api/restaurants/approved'),
    headers: {'Content-Type': 'application/json'},
  );

  if (response.statusCode == 200) {
    List<dynamic> data = jsonDecode(response.body);
    return data.map((json) => Restaurant.fromJson(json)).toList();
  } else {
    throw Exception('Failed to load restaurants');
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

### **Test 2: Approve Restaurant**

**Request:**
```
POST http://localhost:8085/api/restaurants/1/review
Authorization: Bearer SUPER_ADMIN_TOKEN
Content-Type: application/json

{
  "approved": true
}
```

### **Test 3: Reject Restaurant**

**Request:**
```
POST http://localhost:8085/api/restaurants/1/review
Authorization: Bearer SUPER_ADMIN_TOKEN
Content-Type: application/json

{
  "approved": false,
  "rejectionReason": "The commercial registration certificate is expired. Please upload a valid document dated within the last year."
}
```

### **Test 4: Get Approved Restaurants (Customer)**

**Request:**
```
GET http://localhost:8085/api/restaurants/approved
```

---

## Summary

✅ **Restaurant Registration:** Admin uploads all documents, status = PENDING  
✅ **Super Admin Review:** Views documents and makes decision  
✅ **Approval:** Restaurant goes live, email sent to admin  
✅ **Rejection:** Restaurant hidden, email sent with reason  
✅ **Customer Visibility:** Only approved restaurants visible  
✅ **Email Notifications:** Automatic emails for both outcomes  
✅ **Audit Trail:** Tracks who reviewed and when  

The complete approval workflow ensures quality control before restaurants go live! 🎉
