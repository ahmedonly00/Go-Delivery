# Email Verification Implementation for Restaurant Admins

## Overview
This implementation adds email verification for restaurant admins after they upload their menu. The admin must verify their email before accessing the dashboard.

## Implementation Details

### 1. Database Changes
- **New Entity**: `EmailVerificationToken`
  - Location: `src/main/java/com/goDelivery/goDelivery/model/EmailVerificationToken.java`
  - Fields: token, userEmail, restaurantId, expiryDate, used, createdAt
  - Token expires after 24 hours

- **New Repository**: `EmailVerificationTokenRepository`
  - Location: `src/main/java/com/goDelivery/goDelivery/repository/EmailVerificationTokenRepository.java`

### 2. Service Layer Updates

#### EmailVerificationService
- **Location**: `src/main/java/com/goDelivery/goDelivery/service/email/EmailVerificationServiceImpl.java`
- **New Methods**:
  - `sendVerificationEmail(String email, String restaurantName, Long restaurantId)`: Generates token and sends verification email
  - `verifyRestaurantEmail(String token)`: Verifies the token and marks user as verified

#### MenuUploadService
- **Location**: `src/main/java/com/goDelivery/goDelivery/service/MenuUploadService.java`
- **Changes**: After successful menu upload, triggers email verification
- The service now sends a verification email to the restaurant admin's email

### 3. Controller Updates

#### EmailController
- **Location**: `src/main/java/com/goDelivery/goDelivery/controller/EmailController.java`
- **New Endpoint**: `GET /api/emails/verify?token={token}`
  - Verifies the email using the token from the email link
  - Returns success/error message

### 4. Security Configuration

#### EmailVerificationFilter
- **Location**: `src/main/java/com/goDelivery/goDelivery/configSecurity/EmailVerificationFilter.java`
- **Purpose**: Intercepts requests from restaurant admins and blocks access if email is not verified
- **Excluded Paths**: Verification endpoint, auth endpoints, public endpoints
- **Response**: Returns 403 Forbidden with message to verify email

#### SecurityConfig
- **Location**: `src/main/java/com/goDelivery/goDelivery/configSecurity/SecurityConfig.java`
- **Changes**: Added `EmailVerificationFilter` after `JwtAuthenticationFilter`

## Workflow

1. **Menu Upload**:
   - Restaurant admin uploads menu via `POST /api/file-upload/restaurants/{restaurantId}/menu-upload`
   - Menu is processed and saved
   - Verification email is sent to restaurant admin's email

2. **Email Verification**:
   - Admin receives email with verification link
   - Link format: `{baseUrl}/api/emails/verify?token={uniqueToken}`
   - Admin clicks link to verify email

3. **Token Verification**:
   - System validates token (not expired, not used)
   - Marks user as `emailVerified = true` and `setupComplete = true`
   - Token is marked as used

4. **Dashboard Access**:
   - When admin tries to access protected endpoints
   - `EmailVerificationFilter` checks if email is verified
   - If not verified: Returns 403 with message to verify email
   - If verified: Allows access to dashboard and other resources

## Database Migration Required

You need to create a new table for email verification tokens:

```sql
CREATE TABLE email_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    restaurant_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);
```

## Frontend Integration

### After Menu Upload
The frontend should display a message:
```
"Menu uploaded successfully! Please check your email to verify your account before accessing the dashboard."
```

### Verification Link
When user clicks the verification link in email, redirect to:
```
GET {baseUrl}/api/emails/verify?token={token}
```

### Dashboard Access
If user tries to access dashboard without verification:
- Backend returns 403 with message
- Frontend should display: "Please verify your email. Check your inbox for the verification link."

## Configuration

Ensure these properties are set in `application.properties`:
```properties
app.base-url=http://localhost:8085
app.frontend.url=http://localhost:3000
spring.mail.username=your-email@example.com
```

## Testing

1. Upload menu as restaurant admin
2. Check email for verification link
3. Click verification link
4. Try accessing dashboard - should now work
5. Try accessing dashboard before verification - should get 403 error

## Notes

- Tokens expire after 24 hours
- Each user can have only one active token (old tokens are deleted when new one is generated)
- Email verification is only required for RESTAURANT_ADMIN role
- Other roles (CUSTOMER, BIKER, CASHIER, SUPER_ADMIN) are not affected
