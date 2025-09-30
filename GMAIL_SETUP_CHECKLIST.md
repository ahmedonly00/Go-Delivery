# Gmail Setup Checklist - MUST FOLLOW EXACTLY

## 🔴 Current Problem
```
jakarta.mail.AuthenticationFailedException: 
535-5.7.8 Username and Password not accepted
```

**Your current settings:**
- Email: `ahmedndayizeye@gmail.com`
- App Password: `rwysihkijpzpvrvg`

---

## ✅ STEP-BY-STEP FIX

### Step 1: Verify Email Account

**Which email account are you using?**
- ❓ `ahmedndayizeye@gmail.com` (current in config)
- ❓ `ahmedndayizeye45@gmail.com` (previous in config)

**IMPORTANT:** You must generate the App Password for the EXACT email you're using!

---

### Step 2: Check 2-Step Verification

1. Open: https://myaccount.google.com/security
2. Sign in with: **ahmedndayizeye@gmail.com** (your current email)
3. Scroll to "How you sign in to Google"
4. Look for **"2-Step Verification"**

**Is it:**
- ✅ **ON** - Good, proceed to Step 3
- ❌ **OFF** - You MUST enable it first!

**To enable:**
1. Click "2-Step Verification"
2. Click "Get Started"
3. Follow the setup (phone verification)
4. Complete the setup

---

### Step 3: Generate App Password for THIS Email

**CRITICAL:** The App Password MUST be for `ahmedndayizeye@gmail.com`

1. Go to: https://myaccount.google.com/apppasswords
   - Sign in with: **ahmedndayizeye@gmail.com**
   
2. If you see "This setting is not available":
   - 2-Step Verification is NOT enabled → Go back to Step 2

3. If you see the App Passwords page:
   - Click **"Select app"** → Choose **"Mail"**
   - Click **"Select device"** → Choose **"Other (Custom name)"**
   - Type: `GoDelivery Spring Boot`
   - Click **"Generate"**

4. **Copy the 16-character password EXACTLY**
   - Example shown: `abcd efgh ijkl mnop`
   - **Remove all spaces:** `abcdefghijklmnop`
   - This is your App Password

---

### Step 4: Update application.properties

Open: `src/main/resources/application.properties`

Update these TWO lines (lines 45-46):

```properties
spring.mail.username=ahmedndayizeye@gmail.com
spring.mail.password=YOUR_NEW_APP_PASSWORD_WITHOUT_SPACES
```

**Example:**
```properties
spring.mail.username=ahmedndayizeye@gmail.com
spring.mail.password=abcdefghijklmnop
```

**Save the file!**

---

### Step 5: Restart Application

1. **Stop** the current Spring Boot application (if running)
2. **Start** it again
3. Wait for it to fully start

---

### Step 6: Test Email

**Option A: Using Postman**
```
GET http://localhost:8085/api/test/email/send?email=ndayizeye.ahmedy@gmail.com
```

**Option B: Using Browser**
```
http://localhost:8085/api/test/email/send?email=ndayizeye.ahmedy@gmail.com
```

---

## 🔍 Troubleshooting Each Issue

### Issue A: "This setting is not available" when accessing App Passwords

**Cause:** 2-Step Verification is not enabled

**Solution:**
1. Enable 2-Step Verification first
2. Wait 5 minutes
3. Try accessing App Passwords again

---

### Issue B: Still getting authentication error after new App Password

**Possible causes:**
1. ❌ Wrong email in config vs App Password email
2. ❌ Spaces in the password
3. ❌ Copy-paste error
4. ❌ Old password still cached

**Solution:**
```properties
# Make sure email matches EXACTLY
spring.mail.username=ahmedndayizeye@gmail.com

# Make sure password has NO SPACES
spring.mail.password=abcdefghijklmnop
```

**Then:**
- Delete old App Password in Google Account
- Generate NEW one
- Copy it carefully
- Paste into application.properties
- Save file
- Restart application

---

### Issue C: "Please log in via your web browser"

**Cause:** Gmail detected suspicious activity

**Solution:**
1. Open Gmail in browser
2. Sign in to the account
3. Check for security alerts
4. Approve the login
5. Try again

---

### Issue D: Application won't start

**Check logs for:**
```
org.springframework.mail.MailAuthenticationException
```

**This means:** Still wrong credentials

**Double-check:**
- Email is correct
- Password has no spaces
- Password is fresh (just generated)
- Application restarted after change

---

## 🎯 Alternative: Use Different Email for Testing

If you have another Gmail account, try it:

1. Create test email: `your-other-email@gmail.com`
2. Enable 2-Step Verification
3. Generate App Password
4. Update config:
```properties
spring.mail.username=your-other-email@gmail.com
spring.mail.password=new_app_password
```

---

## 📋 Pre-Test Checklist

Before testing, verify ALL of these:

- [ ] Using email: `ahmedndayizeye@gmail.com`
- [ ] 2-Step Verification is **ON** for this email
- [ ] App Password generated for **this exact email**
- [ ] App Password copied **without spaces**
- [ ] `application.properties` updated and **saved**
- [ ] Application **restarted** after changes
- [ ] No typos in email or password
- [ ] Internet connection working
- [ ] Port 587 not blocked

---

## 🚨 Common Mistakes

### Mistake 1: Using regular Gmail password
❌ **Wrong:** `MyGmailPassword123`  
✅ **Correct:** `abcdefghijklmnop` (16-char App Password)

### Mistake 2: Including spaces in App Password
❌ **Wrong:** `abcd efgh ijkl mnop`  
✅ **Correct:** `abcdefghijklmnop`

### Mistake 3: Wrong email
❌ **Wrong:** Config has `email1@gmail.com`, App Password for `email2@gmail.com`  
✅ **Correct:** Both must match exactly

### Mistake 4: Not restarting application
❌ **Wrong:** Change config but don't restart  
✅ **Correct:** Always restart after config change

### Mistake 5: 2-Step Verification not enabled
❌ **Wrong:** Trying to use App Password without 2-Step  
✅ **Correct:** Enable 2-Step Verification first

---

## ✅ Success Indicators

**When it WORKS, you'll see:**

### In Application Logs:
```
DEBUG SMTP: 250 2.1.0 OK
DEBUG SMTP: message successfully delivered to mail server
Email sent successfully to ndayizeye.ahmedy@gmail.com
```

### In Postman/Browser:
```
Email sent successfully to ndayizeye.ahmedy@gmail.com
```

### In Email Inbox:
- New email received
- From: ahmedndayizeye@gmail.com
- Subject: Test Email - MozFood (or similar)
- Check Spam folder if not in Inbox

---

## 🔄 If Nothing Works - Last Resort

### Option 1: Disable Email Test on Startup

Find and comment out the code that sends test email on startup:

**File:** `EmailService.java` or wherever startup email is sent

```java
// Comment out or remove test email sending
// @PostConstruct
// public void sendTestEmail() { ... }
```

This lets app start without email, then test manually.

---

### Option 2: Use Temporary Email Service

For testing only, use a service like Mailtrap:

```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=your_mailtrap_username
spring.mail.password=your_mailtrap_password
```

Get credentials from: https://mailtrap.io

---

## 📞 What to Check NOW

### Immediate Checks:

1. **What is your ACTUAL Gmail address?**
   - ahmedndayizeye@gmail.com? OR
   - ahmedndayizeye45@gmail.com? OR
   - Different one?

2. **Is 2-Step Verification enabled on THAT exact email?**
   - Go check now: https://myaccount.google.com/security

3. **Did you generate the App Password for THAT exact email?**
   - Not another email
   - Not another Google account

4. **Copy the App Password again RIGHT NOW:**
   - Remove ALL spaces
   - Put it in application.properties
   - Save file
   - Restart app

---

## 💡 Quick Test

**Try this RIGHT NOW:**

1. Open: https://myaccount.google.com/apppasswords
2. Sign in with: `ahmedndayizeye@gmail.com`
3. Do you see "App passwords" page or "This setting is not available"?
   - **If you see the page** → Generate new password → Use it
   - **If NOT available** → Enable 2-Step Verification first

---

**Email me or respond with:**
1. Which exact email you're using
2. Whether 2-Step Verification is ON or OFF
3. Any error message you're still seeing

This will help me give you the exact fix!
