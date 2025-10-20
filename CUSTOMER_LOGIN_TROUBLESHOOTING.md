# Customer Login Troubleshooting Guide

## Error: "Invalid email or password"

This error can occur for several reasons when a customer tries to login.

## Common Causes

### 1. Email Not Verified ❌
**Problem**: Customer registered but didn't verify their email with OTP

**Check**:
```sql
SELECT customer_id, full_names, email, is_verified, otp, otp_expiry_time 
FROM customer 
WHERE email = 'your-email@example.com';
```

**Solution**: 
- If `is_verified = false`, customer needs to verify email first
- Resend OTP: `POST /api/auth/resend-otp/{email}`
- Then verify: `POST /api/auth/verify-otp` with email and OTP

### 2. Wrong Password ❌
**Problem**: Customer entered incorrect password

**Check**: Try the password you used during registration

**Solution**: 
- Use password reset if forgotten
- Make sure password matches what was used during registration

### 3. Customer Doesn't Exist ❌
**Problem**: Customer never registered or registration failed

**Check**:
```sql
SELECT * FROM customer WHERE email = 'your-email@example.com';
```

**Solution**: Register first at `/api/customers/register`

### 4. Account Locked/Disabled ❌
**Problem**: Customer account is not active

**Check**:
```sql
SELECT customer_id, email, is_active FROM customer WHERE email = 'your-email@example.com';
```

**Solution**: Contact support to reactivate account

## Step-by-Step Debugging

### Step 1: Check if Customer Exists

**Using Postman/API**:
```http
GET http://localhost:8085/api/customers/check-email/{email}
```

If customer doesn't exist, register first.

### Step 2: Check Email Verification Status

Look at the database or check during login. The error message will tell you:
- "Email not verified. Please verify your email first." → Need OTP verification
- "Invalid email or password" → Wrong password or other issue

### Step 3: Verify Email with OTP

If email not verified:

**Request OTP**:
```http
POST http://localhost:8085/api/auth/resend-otp/your-email@example.com
```

**Verify OTP**:
```http
POST http://localhost:8085/api/auth/verify-otp
Content-Type: application/json

{
  "email": "your-email@example.com",
  "otp": "123456"
}
```

**Success Response**:
```json
{
  "success": true,
  "message": "Email verified successfully! Redirecting to your dashboard...",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "redirectUrl": "http://localhost:3000/customer/dashboard",
  "customerId": 123,
  "customerName": "John Doe",
  "customerEmail": "your-email@example.com"
}
```

After successful OTP verification, you can login normally.

### Step 4: Login

After email is verified:

```http
POST http://localhost:8085/api/auth/login
Content-Type: application/json

{
  "email": "your-email@example.com",
  "password": "your-password"
}
```

**Success Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "id": 123,
  "email": "your-email@example.com",
  "role": "ROLE_CUSTOMER",
  "fullName": "John Doe"
}
```

## Complete Customer Flow

### 1. Registration
```http
POST http://localhost:8085/api/customers/register
Content-Type: application/json

{
  "fullNames": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phoneNumber": "1234567890"
}
```

### 2. Check Email for OTP
- Customer receives email with 6-digit OTP
- OTP is valid for 5 minutes

### 3. Verify OTP
```http
POST http://localhost:8085/api/auth/verify-otp
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "123456"
}
```

**This step**:
- ✅ Marks email as verified
- ✅ Generates JWT token (auto-login)
- ✅ Returns dashboard URL
- ✅ Customer can now login anytime

### 4. Login (After Verification)
```http
POST http://localhost:8085/api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

## Quick Fixes

### Fix 1: Manually Verify Customer (Database)

If you need to bypass OTP for testing:

```sql
UPDATE customer 
SET is_verified = true, otp = NULL, otp_expiry_time = NULL 
WHERE email = 'your-email@example.com';
```

Then try login again.

### Fix 2: Reset Password

If password is forgotten:

```http
POST http://localhost:8085/api/auth/forgot-password
Content-Type: application/json

{
  "email": "your-email@example.com"
}
```

Check email for reset link, then set new password.

### Fix 3: Check Password Encoding

Passwords are encrypted with BCrypt. Make sure:
- Password during registration is encrypted
- Password during login is compared correctly
- No extra spaces or characters

## Testing Checklist

✅ **Customer registered successfully**
✅ **Email received with OTP**
✅ **OTP verified successfully** (is_verified = true)
✅ **Password is correct**
✅ **Account is active**
✅ **Using correct login endpoint** (`/api/auth/login`)

## Common Mistakes

❌ **Trying to login before OTP verification**
   → Solution: Verify email first

❌ **Using wrong password**
   → Solution: Use exact password from registration or reset

❌ **OTP expired (after 5 minutes)**
   → Solution: Request new OTP

❌ **Typo in email address**
   → Solution: Check email spelling

❌ **Using restaurant/admin login for customer**
   → Solution: All users use same `/api/auth/login` endpoint

## Database Queries for Debugging

### Check Customer Status
```sql
SELECT 
    customer_id,
    full_names,
    email,
    is_verified,
    is_active,
    otp,
    otp_expiry_time,
    created_at
FROM customer 
WHERE email = 'your-email@example.com';
```

### Check All Unverified Customers
```sql
SELECT customer_id, full_names, email, created_at 
FROM customer 
WHERE is_verified = false 
ORDER BY created_at DESC;
```

### Manually Verify All Customers (Testing Only)
```sql
UPDATE customer 
SET is_verified = true, otp = NULL, otp_expiry_time = NULL 
WHERE is_verified = false;
```

## Error Messages Explained

| Error Message | Cause | Solution |
|--------------|-------|----------|
| "Email not verified. Please verify your email first." | OTP not completed | Verify email with OTP |
| "Invalid email or password" | Wrong password or email | Check credentials |
| "User not found with email: ..." | Customer doesn't exist | Register first |
| "Account is locked or disabled" | Account inactive | Contact support |
| "Invalid or expired OTP" | Wrong OTP or expired | Request new OTP |

## API Endpoints Summary

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/customers/register` | POST | Public | Register new customer |
| `/api/auth/verify-otp` | POST | Public | Verify email with OTP |
| `/api/auth/resend-otp/{email}` | POST | Public | Resend OTP |
| `/api/auth/login` | POST | Public | Login (after verification) |
| `/api/auth/forgot-password` | POST | Public | Request password reset |

## Your Specific Case

For email: `ndayizeye.ahmedy@gmail.com`

**Steps to fix**:

1. **Check if customer exists and is verified**:
   ```sql
   SELECT customer_id, email, is_verified FROM customer 
   WHERE email = 'ndayizeye.ahmedy@gmail.com';
   ```

2. **If is_verified = false**:
   - Option A: Verify with OTP (recommended)
     ```http
     POST http://localhost:8085/api/auth/resend-otp/ndayizeye.ahmedy@gmail.com
     ```
     Then verify with the OTP received
   
   - Option B: Manually verify in database (testing only)
     ```sql
     UPDATE customer 
     SET is_verified = true, otp = NULL, otp_expiry_time = NULL 
     WHERE email = 'ndayizeye.ahmedy@gmail.com';
     ```

3. **If is_verified = true but still can't login**:
   - Password might be wrong
   - Try password reset or check the password you used during registration

4. **Try login again**:
   ```http
   POST http://localhost:8085/api/auth/login
   Content-Type: application/json

   {
     "email": "ndayizeye.ahmedy@gmail.com",
     "password": "your-actual-password"
   }
   ```

---

**Most Likely Issue**: Email not verified. Complete OTP verification first, then login will work! 🎯
