# Configuration Verification Checklist

## ✅ Configuration Files Analysis

### 1. **application.properties** - CORRECT ✅

The file is properly configured to read from environment variables with fallback defaults:

```properties
# Database
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/go_delivery_db?useSSL=false&allowPublicKeyRetrieval=true}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:password}

# Email
spring.mail.username=${MAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your-app-password}

# JWT
jwt.secret=${JWT_SECRET:default-secret-key-change-in-production}
jwt.expiration=${JWT_EXPIRATION:86400000}

# URLs
app.base-url=${APP_BASE_URL:http://localhost:8085}
app.frontend.url=${FRONTEND_URL:http://localhost:3000}
server.port=${SERVER_PORT:8085}

# Super Admin
app.superadmin.email=${SUPER_ADMIN_EMAIL:admin@godelivery.com}
app.superadmin.password=${SUPER_ADMIN_PASSWORD:Admin@123}
```

**Format**: `${ENV_VARIABLE:default-value}`
- Reads from `.env` file first
- Falls back to default if not found

---

## 2. **Your .env File** - NEEDS VERIFICATION ⚠️

Your `.env` file should match this structure exactly:

```env
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/go_delivery_db?useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=postgres
DB_PASSWORD=your_actual_postgres_password

# Email Configuration
MAIL_USERNAME=your-actual-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password

# JWT Configuration
JWT_SECRET=your-secret-key-at-least-256-bits-long
JWT_EXPIRATION=86400000

# Super Admin Configuration
SUPER_ADMIN_EMAIL=admin@godelivery.com
SUPER_ADMIN_PASSWORD=Admin@123

# Application Configuration
APP_BASE_URL=http://localhost:8085
SERVER_PORT=8085

# Frontend URL (for email links)
FRONTEND_URL=http://localhost:3000
```

---

## 3. **Critical Checks**

### ✅ Check 1: Variable Names Match
Verify that variable names in `.env` EXACTLY match those in `application.properties`:

| application.properties | .env file | Status |
|------------------------|-----------|--------|
| `${DB_URL:...}` | `DB_URL=...` | ✅ Match |
| `${DB_USERNAME:...}` | `DB_USERNAME=...` | ✅ Match |
| `${DB_PASSWORD:...}` | `DB_PASSWORD=...` | ✅ Match |
| `${MAIL_USERNAME:...}` | `MAIL_USERNAME=...` | ✅ Match |
| `${MAIL_PASSWORD:...}` | `MAIL_PASSWORD=...` | ✅ Match |
| `${JWT_SECRET:...}` | `JWT_SECRET=...` | ✅ Match |
| `${JWT_EXPIRATION:...}` | `JWT_EXPIRATION=...` | ✅ Match |
| `${APP_BASE_URL:...}` | `APP_BASE_URL=...` | ✅ Match |
| `${FRONTEND_URL:...}` | `FRONTEND_URL=...` | ✅ Match |
| `${SERVER_PORT:...}` | `SERVER_PORT=...` | ✅ Match |
| `${SUPER_ADMIN_EMAIL:...}` | `SUPER_ADMIN_EMAIL=...` | ✅ Match |
| `${SUPER_ADMIN_PASSWORD:...}` | `SUPER_ADMIN_PASSWORD=...` | ✅ Match |

### ⚠️ Check 2: .env File Format

**CRITICAL RULES:**

1. **NO spaces around `=`**
   ```env
   ✅ CORRECT: DB_PASSWORD=mypassword
   ❌ WRONG:   DB_PASSWORD = mypassword
   ❌ WRONG:   DB_PASSWORD= mypassword
   ❌ WRONG:   DB_PASSWORD =mypassword
   ```

2. **NO quotes around values** (unless the value itself contains quotes)
   ```env
   ✅ CORRECT: DB_PASSWORD=mypassword123
   ❌ WRONG:   DB_PASSWORD="mypassword123"
   ❌ WRONG:   DB_PASSWORD='mypassword123'
   ```

3. **NO trailing spaces**
   ```env
   ✅ CORRECT: DB_PASSWORD=mypassword
   ❌ WRONG:   DB_PASSWORD=mypassword   
   ```

4. **Comments start with #**
   ```env
   ✅ CORRECT: # This is a comment
   ✅ CORRECT: DB_PASSWORD=mypassword  # inline comment OK
   ```

### ⚠️ Check 3: Actual Values

Open your `.env` file and verify these values are NOT defaults:

```env
# ❌ These should be changed:
DB_PASSWORD=your_database_password          # Change this!
MAIL_USERNAME=your-email@gmail.com          # Change this!
MAIL_PASSWORD=your-gmail-app-password       # Change this!
JWT_SECRET=your-secret-key-at-least-256-bits # Change this!

# ✅ These should be your actual values:
DB_PASSWORD=YourActualPostgresPassword
MAIL_USERNAME=youremail@gmail.com
MAIL_PASSWORD=abcdefghijklmnop
JWT_SECRET=a-very-long-random-secret-key-at-least-256-bits-long-for-security
```

---

## 4. **Database Configuration Test**

### Test PostgreSQL Connection:

```bash
# Windows (PowerShell)
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d go_delivery_db

# If database doesn't exist, create it:
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres
CREATE DATABASE go_delivery_db;
\q
```

### Verify Database Password:
The password in `.env` must match your PostgreSQL password:
```env
DB_PASSWORD=the-password-you-use-for-postgres
```

---

## 5. **Email Configuration Test**

### Gmail App Password Requirements:

1. **Enable 2-Factor Authentication** on your Google account
2. **Generate App Password**:
   - Go to: https://myaccount.google.com/security
   - Search for "App passwords"
   - Create new password for "Mail"
   - Copy the 16-character code (e.g., `abcd efgh ijkl mnop`)
   - **Remove all spaces**: `abcdefghijklmnop`

3. **Update .env**:
```env
MAIL_USERNAME=youremail@gmail.com
MAIL_PASSWORD=abcdefghijklmnop
```

---

## 6. **Startup Verification**

When you start the application, check for these logs:

### ✅ Success Indicators:

```
? Environment variables loaded from .env file
============================================================
📧 EMAIL CONFIGURATION LOADED
============================================================
From Email: youremail@gmail.com
SMTP Host: smtp.gmail.com
SMTP Port: 587
============================================================
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Database version: 16.10
Started GoDeliveryApplication in X.XX seconds
```

### ❌ Failure Indicators:

```
⚠️  WARNING: Email is not properly configured!
From Email: ❌ NOT CONFIGURED
FATAL: password authentication failed for user "postgres"
Unable to open JDBC Connection
```

---

## 7. **Quick Diagnostic Commands**

### Check if .env is being loaded:
Look for this in startup logs:
```
? Environment variables loaded from .env file
```

### Test database connection:
```powershell
# Windows
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d go_delivery_db
```

### View application logs:
```powershell
Get-Content "logs\application.log" -Tail 50
```

---

## 8. **Common Issues & Solutions**

### Issue 1: "password authentication failed for user postgres"
**Cause**: Wrong database password in `.env`
**Solution**: 
1. Find your PostgreSQL password
2. Update `DB_PASSWORD` in `.env`
3. Restart application

### Issue 2: Email not configured warning
**Cause**: Wrong email credentials in `.env`
**Solution**:
1. Generate Gmail App Password
2. Update `MAIL_USERNAME` and `MAIL_PASSWORD` in `.env`
3. Remove all spaces from app password
4. Restart application

### Issue 3: .env file not loaded
**Cause**: File not in project root or wrong format
**Solution**:
1. Verify `.env` is in: `d:\Desktop\Go Delivery Project\goDelivery\.env`
2. Check file has no extension (not `.env.txt`)
3. Verify `DotenvConfig.java` is loading it

### Issue 4: Variables not being read
**Cause**: Spaces around `=` or quotes around values
**Solution**:
```env
# Wrong:
DB_PASSWORD = "mypassword"

# Correct:
DB_PASSWORD=mypassword
```

---

## 9. **Final Checklist**

Before starting the application, verify:

- [ ] `.env` file exists in project root
- [ ] All variable names match between `.env` and `application.properties`
- [ ] No spaces around `=` in `.env`
- [ ] No quotes around values in `.env`
- [ ] Database password is correct
- [ ] Gmail App Password is correct (16 chars, no spaces)
- [ ] PostgreSQL is running
- [ ] Database `go_delivery_db` exists

---

## 10. **Test After Configuration**

1. **Start application**
2. **Check startup logs** for email configuration
3. **Register a test customer**
4. **Verify OTP email is sent**
5. **Check logs** for success messages

---

## Need Help?

If you're still having issues, share:
1. First 100 lines of startup logs
2. Any error messages
3. Confirmation that PostgreSQL is running
4. Confirmation that .env file exists and has correct format
