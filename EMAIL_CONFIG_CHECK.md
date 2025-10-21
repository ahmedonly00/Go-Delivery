# Email Configuration Checklist

## Issue: OTP saved to database but email not sent

This means the application is working, but the email service is failing silently. Here's how to diagnose:

## Step 1: Check Your .env File

Open your `.env` file and verify these settings:

```env
MAIL_USERNAME=your-actual-email@gmail.com
MAIL_PASSWORD=your-16-character-app-password
```

### Common Issues:

1. **Using regular Gmail password instead of App Password**
   - ❌ Wrong: Your normal Gmail password
   - ✅ Correct: 16-character app password (e.g., `abcd efgh ijkl mnop`)

2. **Extra spaces in the password**
   - The app password should have NO spaces
   - Copy it directly without any formatting

3. **Wrong email format**
   - Must be a valid Gmail address
   - Must match the account where you generated the app password

## Step 2: Generate Gmail App Password (If Not Done)

1. Go to: https://myaccount.google.com/security
2. Enable **2-Step Verification** (required!)
3. Go to **App passwords** section
4. Select **Mail** and your device
5. Click **Generate**
6. Copy the 16-character password (no spaces)
7. Paste it in your `.env` file as `MAIL_PASSWORD`

## Step 3: Test Email Configuration

### Option A: Use the Test Endpoint

```bash
# In Postman or curl:
POST http://localhost:8085/api/emails/test
Content-Type: application/json

{
  "to": "your-test-email@gmail.com"
}
```

### Option B: Register a New Customer

```bash
POST http://localhost:8085/api/customers/register
Content-Type: application/json

{
  "fullNames": "Test User",
  "email": "test@example.com",
  "password": "Test@123",
  "confirmPassword": "Test@123",
  "phoneNumber": "1234567890",
  "location": "Test Location"
}
```

## Step 4: Check Application Logs

After testing, look for these log messages:

### Success Indicators:
```
✅ Attempting to send OTP email to: customer@example.com
✅ Starting OTP email preparation for: customer@example.com
✅ OTP email successfully sent to: customer@example.com
```

### Failure Indicators:
```
❌ Failed to submit OTP email request
❌ MessagingException while sending OTP email
❌ Authentication failed
❌ Connection timeout
```

## Step 5: Common Error Solutions

### Error: "Authentication failed"
**Cause**: Wrong credentials or not using app password
**Solution**: 
- Generate a new Gmail App Password
- Update `.env` with the new password
- Restart the application

### Error: "Connection timeout"
**Cause**: Firewall blocking SMTP port 587
**Solution**:
- Check your firewall settings
- Try using port 465 with SSL (update application.properties)

### Error: No logs at all
**Cause**: Email service not being called or async issue
**Solution**:
- Check if `AsyncConfig` is loaded
- Verify `EmailService` is a Spring bean
- Check if the registration endpoint is being hit

## Step 6: Verify Environment Variables Are Loaded

Add this temporary log to see if credentials are loaded:

In `EmailService.java`, add after line 29:
```java
@PostConstruct
public void init() {
    log.info("Email config loaded - From: {}", fromEmail);
    log.info("SMTP Host: smtp.gmail.com, Port: 587");
}
```

This will show in logs on startup if email is configured.

## Step 7: Manual Test

Try registering a NEW customer (not one that already exists):

1. Use a unique email address
2. Watch the console/logs in real-time
3. Look for the OTP email logs
4. Check your email inbox (and spam folder!)

## Quick Diagnostic Command

Run this in PowerShell to see recent email-related logs:

```powershell
Get-Content "logs\application.log" -Tail 500 | Select-String -Pattern "email|OTP|mail|smtp" -Context 2
```

## If Still Not Working

1. **Verify .env is being loaded**: Check startup logs for "Environment variables loaded from .env file"
2. **Check email credentials**: Try logging into Gmail with those credentials
3. **Test SMTP connection**: Use a tool like Telnet to test SMTP connection
4. **Check Gmail security**: Ensure "Less secure app access" is not blocking (though app passwords should work)

## Need More Help?

Share the following information:
1. Startup logs (first 50 lines)
2. Registration attempt logs
3. Any error messages from logs
4. Confirmation that .env file has correct format (don't share actual passwords!)
