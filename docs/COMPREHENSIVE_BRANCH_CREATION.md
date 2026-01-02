# Comprehensive Branch Creation System

## Overview
The branch creation system has been enhanced to be as comprehensive as restaurant creation. Restaurant admins can now create fully-featured branches with all necessary details including location, operating hours, delivery settings, social media links, and more.

## Key Features

### 1. **Detailed Branch Information**
- **Location**: Full address with latitude/longitude
- **Contact**: Phone, email, website
- **Operating Hours**: Day-specific opening/closing times
- **Description**: Detailed branch description
- **Logo**: Branch logo upload

### 2. **Delivery Settings**
- Delivery availability toggle
- Delivery radius (in km)
- Minimum order amount
- Delivery fee configuration

### 3. **Social Media Integration**
- Facebook URL
- Instagram URL
- Twitter URL

### 4. **Branch Features**
- Parking availability
- WiFi access
- Outdoor seating
- Reservation acceptance

### 5. **Branch Manager Creation**
- Automatic creation of branch manager account
- Credentials provided during branch creation
- Manager gets full access to branch operations

### 6. **Document Management**
- Business document upload
- Operating license upload
- Secure file storage

## API Endpoints

### Create Branch
```
POST /api/v1/branches/create/{restaurantId}
Content-Type: multipart/form-data

Request Parts:
- branchData: JSON with all branch details
- logo: Optional branch logo image
- documents: Optional array of business documents
```

### Update Branch
```
PUT /api/v1/branches/update/{branchId}
Content-Type: multipart/form-data

Request Parts:
- branchData: JSON with updated branch details
- logo: Optional new logo image
```

### Get Branch Details
```
GET /api/v1/branches/{branchId}
```

### Get Restaurant Branches
```
GET /api/v1/branches/restaurant/{restaurantId}
```

### Activate/Deactivate Branch
```
POST /api/v1/branches/{branchId}/activate
POST /api/v1/branches/{branchId}/deactivate
```

## Request Example

### Branch Creation Request
```json
{
  "branchName": "Downtown Branch",
  "address": "123 Main Street",
  "addressLine2": "Suite 100",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "phoneNumber": "+1234567890",
  "email": "downtown@restaurant.com",
  "website": "https://downtown.restaurant.com",
  "description": "Our flagship downtown location",
  "mondayOpen": "09:00",
  "mondayClose": "22:00",
  "tuesdayOpen": "09:00",
  "tuesdayClose": "22:00",
  "wednesdayOpen": "09:00",
  "wednesdayClose": "22:00",
  "thursdayOpen": "09:00",
  "thursdayClose": "22:00",
  "fridayOpen": "09:00",
  "fridayClose": "23:00",
  "saturdayOpen": "10:00",
  "saturdayClose": "23:00",
  "sundayOpen": "10:00",
  "sundayClose": "22:00",
  "deliveryAvailable": true,
  "deliveryRadius": 5.0,
  "minimumOrderAmount": 20.0,
  "deliveryFee": 2.99,
  "facebookUrl": "https://facebook.com/downtown-branch",
  "instagramUrl": "https://instagram.com/downtown-branch",
  "twitterUrl": "https://twitter.com/downtown-branch",
  "hasParking": true,
  "hasWifi": true,
  "hasOutdoorSeating": true,
  "acceptsReservations": true,
  "managerName": "John Doe",
  "managerEmail": "john.doe@restaurant.com",
  "managerPhone": "+1234567890",
  "managerPassword": "SecurePassword123!",
  "initialMenuCategories": ["Appetizers", "Main Courses", "Desserts"]
}
```

## Database Schema

### Branches Table - New Fields
```sql
-- Contact Information
email VARCHAR(255)
website VARCHAR(255)
logo_url VARCHAR(500)

-- Delivery Settings
delivery_available BOOLEAN DEFAULT FALSE
delivery_radius FLOAT
minimum_order_amount FLOAT
delivery_fee FLOAT

-- Social Media
facebook_url VARCHAR(500)
instagram_url VARCHAR(500)
twitter_url VARCHAR(500)

-- Features
has_parking BOOLEAN
has_wifi BOOLEAN
has_outdoor_seating BOOLEAN
accepts_reservations BOOLEAN

-- Ratings
average_rating DOUBLE DEFAULT 0.0
review_count INTEGER DEFAULT 0
```

## Workflow

### 1. Restaurant Admin Creates Branch
1. Navigate to branch creation page
2. Fill in comprehensive branch details
3. Upload branch logo and documents
4. Set up branch manager credentials
5. Submit for creation

### 2. System Processing
1. Validates all branch information
2. Creates branch with PENDING status
3. Uploads and stores files
4. Creates branch manager account
5. Sets up initial menu categories

### 3. Post-Creation
1. Branch manager receives login credentials
2. Branch is ready for setup completion
3. Restaurant admin can approve/activate branch
4. Branch becomes operational

## Security

### Access Control
- Only restaurant admins can create branches
- Branch managers can only update their assigned branch
- All file uploads are validated and stored securely
- Document access is restricted to authorized users

### Validation Rules
- Branch name must be unique within restaurant
- Phone numbers must be valid format
- Email addresses must be valid
- Operating hours must be logical (close > open)

## File Upload Structure
```
uploads/
├── branches/
│   ├── logo/
│   │   └── {branchId}/
│   └── documents/
│       └── {branchId}/
```

## Integration Points

### Existing Systems
- **Authentication**: Branch managers use same auth system
- **Menu System**: Initial categories created during setup
- **Order System**: Branch-specific order processing
- **Payment System**: Branch payment processing
- **Analytics**: Branch-specific reporting

### Future Enhancements
- Branch-specific promotions
- Multi-branch inventory management
- Branch transfer functionality
- Branch performance comparison
- Automated branch suggestions based on location

## Best Practices

1. **Data Validation**: Always validate branch data before saving
2. **File Management**: Implement proper file cleanup for deleted branches
3. **Performance**: Use pagination for branch listings
4. **Security**: Regularly audit branch access permissions
5. **Monitoring**: Track branch creation and performance metrics

## Testing Considerations

1. Test branch creation with all field combinations
2. Verify file upload functionality
3. Test branch manager account creation
4. Validate operating hours logic
5. Test location-based features
6. Verify approval workflow
