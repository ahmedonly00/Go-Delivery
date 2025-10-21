# Email Configuration Troubleshooting Guide

## Issue Fixed
The OTP was being created in the database but emails were not being sent.

## Root Cause
The `@Async` method in `OTPService.sendOtpEmail()` was being called from within the same class (`generateAndSaveOTP()`). Spring's `@Async` annotation doesn't work when methods are called internally within the same class because the proxy mechanism is bypassed.

## Changes Made

### 1. Fixed OTPService.java
- **Removed** the internal `@Async` wrapper method `sendOtpEmail()`
- **Changed** to call `emailService.sendOtpEmail()` directly (which is already `@Async`)
- **Added** better error logging

### 2. Enhanced EmailService.java
- **Added** detailed logging at each step of email sending
- **Added** better exception handling with specific error messages
- **Added** emoji markers (✅/❌) for easy log scanning

## Email Configuration Checklist

### Step 1: Verify .env File
Ensure your `.env` file has the correct Gmail credentials:

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

⚠️ **Important**: Use Gmail App Password, NOT your regular Gmail password!

### Step 2: Generate Gmail App Password
1. Go to Google Account Settings → Security
2. Enable 2-Factor Authentication (required)
3. Go to "App passwords" section
4. Generate a new app password for "Mail"
5. Copy the 16-character password (no spaces)
6. Use this in your `.env` file

### Step 3: Test Email Configuration

#### Option A: Using the Test Endpoint
```bash
curl -X POST http://localhost:8085/api/emails/test \
  -H "Content-Type: application/json" \
  -d '{"to": "your-test-email@gmail.com"}'
```

#### Option B: Check Application Logs
After registering a new customer, look for these log messages:

**Success indicators:**
```
✅ OTP email successfully sent to: customer@example.com
```

**Failure indicators:**
```
❌ MessagingException while sending OTP email to ...
❌ Unexpected error while sending OTP email to ...
```

### Step 4: Common Issues & Solutions

#### Issue: "Authentication failed"
**Solution**: 
- Verify you're using Gmail App Password, not regular password
- Check if 2FA is enabled on your Google account
- Ensure no extra spaces in the password

#### Issue: "Connection timeout"
**Solution**:
- Check your internet connection
- Verify firewall isn't blocking port 587
- Try using port 465 with SSL instead

#### Issue: "Invalid Addresses"
**Solution**:
- Verify the recipient email format is correct
- Check if `spring.mail.username` is set correctly in `.env`

#### Issue: Emails not sending but no errors
**Solution**:
- Check if async is enabled (should be via `@EnableAsync` in `AsyncConfig`)
- Look for async thread pool errors in logs
- Verify `EmailService` is a Spring-managed bean

## Monitoring Email Sending

### Check Logs
The application logs email operations at these levels:
- `INFO`: Email sending attempts and successes
- `DEBUG`: Detailed SMTP communication
- `ERROR`: Failures with stack traces

### Log Locations
- Console output during development
- `logs/application.log` file

### Key Log Patterns to Search For
```bash
# Search for OTP email attempts
grep "Attempting to send OTP email" logs/application.log

# Search for successful sends
grep "✅ OTP email successfully sent" logs/application.log

# Search for failures
grep "❌" logs/application.log
```

## Testing the Full Flow

1. **Register a new customer** via API:
```bash
POST http://localhost:8085/api/customers/register
{
  "fullNames": "Test User",
  "email": "test@example.com",
  "password": "Test@123",
  "confirmPassword": "Test@123",
  "phoneNumber": "1234567890",
  "location": "Test Location"
}
```

2. **Check logs** for email sending confirmation

3. **Check email inbox** for OTP (check spam folder too!)

4. **Verify OTP** via API:
```bash
POST http://localhost:8085/api/otp/verify-otp
{
  "email": "test@example.com",
  "otp": "123456"
}
```

## Email Template Location
The OTP email template is located at:
```
src/main/resources/templates/emails/otp-verification.html
```

## Configuration Files
- **Mail Config**: `src/main/java/com/goDelivery/goDelivery/config/MailConfig.java`
- **Email Service**: `src/main/java/com/goDelivery/goDelivery/service/email/EmailService.java`
- **OTP Service**: `src/main/java/com/goDelivery/goDelivery/service/OTPService.java`
- **Properties**: `src/main/resources/application.properties`
- **Environment**: `.env` (not in git)

## Next Steps After Fix

1. Restart your application
2. Try registering a new customer
3. Monitor the logs for the new detailed messages
4. Check if the email arrives
5. If still failing, check the specific error message in logs and refer to "Common Issues" above

## Support
If issues persist after following this guide:
1. Check the full stack trace in logs
2. Verify all environment variables are loaded correctly
3. Test with the `/api/emails/test` endpoint first
4. Ensure Gmail account settings allow "Less secure app access" or use App Passwords
