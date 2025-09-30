# Email Troubleshooting Guide

## Problem: Emails Not Being Sent

### Current Error
```
jakarta.mail.AuthenticationFailedException: 535-5.7.8 Username and Password not accepted
```

This means Gmail is rejecting your authentication credentials.

---

## ✅ Solution Steps

### 1. Enable 2-Step Verification in Gmail

**Required for App Passwords**

1. Go to https://myaccount.google.com/
2. Click **Security** (left sidebar)
3. Find **2-Step Verification**
4. Click **Get Started** and follow the setup

---

### 2. Generate Gmail App Password

**After 2-Step Verification is enabled:**

1. Go to https://myaccount.google.com/apppasswords
   - Or: Google Account → Security → 2-Step Verification → App passwords

2. You'll be asked to sign in again

3. Select:
   - **Select app:** Mail
   - **Select device:** Other (Custom name)
   - Enter: `GoDelivery` or `Spring Boot App`

4. Click **Generate**

5. **Copy the 16-character password** (spaces will be shown but don't include them)
   - Example: `abcd efgh ijkl mnop` → Use as `abcdefghijklmnop`

---

### 3. Update application.properties

Replace `YOUR_NEW_16_CHAR_APP_PASSWORD` with the generated password:

```properties
spring.mail.username=ahmedndayizeye45@gmail.com
spring.mail.password=abcdefghijklmnop
```

**Important:**
- ❌ Don't use your regular Gmail password
- ✅ Use the 16-character App Password
- ❌ Don't add spaces in the password
- ✅ Enter it as one continuous string

---

### 4. Restart Spring Boot Application

After updating the password:
1. Stop the application
2. Restart it
3. Test email sending again

---

## Testing Email

### Using Postman

```http
GET http://localhost:8085/api/test/email/send?email=ndayizeye.ahmedy@gmail.com
```

**Expected Success Response:**
```
Email sent successfully to ndayizeye.ahmedy@gmail.com
```

**Check:**
1. Inbox of recipient email
2. Spam/Junk folder
3. Application logs for errors

---

## Common Issues & Solutions

### Issue 1: "Username and Password not accepted"

**Cause:** Invalid or expired app password

**Solution:**
1. Delete old app password in Google Account
2. Generate a new one
3. Update application.properties
4. Restart application

---

### Issue 2: "535-5.7.8 Please log in via your web browser"

**Cause:** Gmail security blocking

**Solution:**
1. Enable 2-Step Verification
2. Use App Password (not regular password)
3. Allow less secure apps (not recommended, use App Password instead)

---

### Issue 3: Email sent but not received

**Cause:** Email in spam or Gmail blocking

**Solution:**
1. Check spam/junk folder
2. Check Gmail's "All Mail" folder
3. Add sender to contacts
4. Wait a few minutes (delays happen)

---

### Issue 4: "Connection timeout"

**Cause:** Network/firewall issue

**Solution:**
1. Check internet connection
2. Verify port 587 is not blocked
3. Try port 465 with SSL:
```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
```

---

### Issue 5: Application says "sent" but logs show errors

**Cause:** Async email sending hiding errors

**Check logs:**
```bash
# Look for email errors in logs
tail -f logs/application.log | grep -i "email\|mail\|smtp"
```

---

## Alternative Configuration (Port 465 with SSL)

If port 587 doesn't work, try SSL on port 465:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=ahmedndayizeye45@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
spring.mail.protocol=smtp
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
```

---

## Verification Checklist

Before testing, verify:

- [ ] 2-Step Verification enabled in Google Account
- [ ] App Password generated (16 characters)
- [ ] App Password copied correctly (no spaces)
- [ ] application.properties updated with new password
- [ ] Spring Boot application restarted
- [ ] Port 587 or 465 not blocked by firewall
- [ ] Internet connection working

---

## Testing Steps

### 1. Test Email Endpoint
```bash
curl -X GET "http://localhost:8085/api/test/email/send?email=your-email@gmail.com"
```

### 2. Check Logs
```bash
# Look for success
grep "Email sent successfully" logs/application.log

# Look for errors
grep -i "error\|exception\|failed" logs/application.log
```

### 3. Check Email
- Inbox
- Spam/Junk
- All Mail
- Wait 2-3 minutes

---

## Gmail Limits

**Be aware of Gmail sending limits:**

- **Per day:** 500 emails
- **Per minute:** ~20 emails
- **Exceeding limits:** Temporary suspension

**For production:**
- Use professional email service (SendGrid, AWS SES, Mailgun)
- Gmail App Password is for development/testing only

---

## Debugging Mode

Enable detailed SMTP debugging:

```properties
spring.mail.properties.mail.debug=true
logging.level.org.springframework.mail=DEBUG
```

This will show detailed SMTP conversation in logs:
```
DEBUG: JavaMail version 2.0.1
DEBUG: successfully connected to host "smtp.gmail.com", port: 587
DEBUG SMTP: AUTH LOGIN succeeded
DEBUG SMTP: message successfully delivered to mail server
```

---

## Current Configuration

**Your settings:**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=ahmedndayizeye45@gmail.com
spring.mail.password=[UPDATE THIS WITH APP PASSWORD]
```

**Sending from:** ahmedndayizeye45@gmail.com  
**Test recipient:** ndayizeye.ahmedy@gmail.com

---

## Quick Fix Steps

1. **Generate new App Password:**
   - Visit: https://myaccount.google.com/apppasswords
   - Generate password for "Mail" / "GoDelivery"
   - Copy 16-character code

2. **Update application.properties:**
   ```properties
   spring.mail.password=THE_NEW_16_CHAR_CODE
   ```

3. **Restart application:**
   - Stop current run
   - Start again

4. **Test:**
   ```
   GET http://localhost:8085/api/test/email/send?email=ndayizeye.ahmedy@gmail.com
   ```

5. **Check inbox and spam folder**

---

## If Still Not Working

### Option 1: Try Different Email Service

Use a test email service like Mailtrap (for development):

```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=your_mailtrap_username
spring.mail.password=your_mailtrap_password
```

### Option 2: Use SendGrid (Production)

```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=your_sendgrid_api_key
```

### Option 3: Check Gmail Account

- Ensure account is not suspended
- Check for security alerts
- Verify email is not in "Paused" state

---

## Success Indicators

**When it works, you'll see:**

1. **In Postman:**
   ```
   Email sent successfully to ndayizeye.ahmedy@gmail.com
   ```

2. **In Logs:**
   ```
   DEBUG SMTP: message successfully delivered to mail server
   Sending email to: ndayizeye.ahmedy@gmail.com
   Email sent successfully after X attempts
   ```

3. **In Email:**
   - Subject: "Test Email - MozFood" (or similar)
   - From: ahmedndayizeye45@gmail.com
   - Content: Welcome email template

---

## Support Links

- **Gmail App Passwords:** https://support.google.com/accounts/answer/185833
- **2-Step Verification:** https://support.google.com/accounts/answer/185839
- **Gmail SMTP Settings:** https://support.google.com/mail/answer/7126229

---

**Last Updated:** 2025-09-30  
**Status:** Waiting for Gmail App Password update
