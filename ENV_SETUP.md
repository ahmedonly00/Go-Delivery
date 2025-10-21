# Environment Variables Setup

## Overview

This project uses a `.env` file to store sensitive configuration data like database credentials, API keys, and secrets. This keeps sensitive information out of version control.

## Setup Instructions

### 1. Create Your `.env` File

Copy the example file and fill in your actual values:

```bash
cp .env.example .env
```

Or manually create a `.env` file in the project root with the following variables:

```env
# Database Configuration
DB_URL=jdbc:postgresql://your-host:5432/go_delivery_db?useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Email Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# JWT Configuration
JWT_SECRET=your-secret-key-at-least-256-bits
JWT_EXPIRATION=86400000

# Super Admin Configuration
SUPER_ADMIN_EMAIL=admin@godelivery.com
SUPER_ADMIN_PASSWORD=Admin@123

# Application Configuration
APP_BASE_URL=http://localhost:8085
SERVER_PORT=8085

# Frontend URL
FRONTEND_URL=http://localhost:3000
```

### 2. Configure Your Values

#### Database Configuration
- **DB_URL**: Your PostgreSQL database connection URL
- **DB_USERNAME**: Database username
- **DB_PASSWORD**: Database password

#### Email Configuration (Gmail)
- **MAIL_USERNAME**: Your Gmail address
- **MAIL_PASSWORD**: Gmail App Password (not your regular password)
  - Generate at: https://myaccount.google.com/apppasswords

#### JWT Configuration
- **JWT_SECRET**: A secure random string (at least 256 bits)
  - Generate using: `openssl rand -hex 32`
- **JWT_EXPIRATION**: Token expiration time in milliseconds (default: 24 hours)

#### Admin Configuration
- **SUPER_ADMIN_EMAIL**: Super admin email address
- **SUPER_ADMIN_PASSWORD**: Super admin password

#### Application URLs
- **APP_BASE_URL**: Backend API base URL
- **SERVER_PORT**: Port for the Spring Boot application
- **FRONTEND_URL**: Frontend application URL (for email links)

### 3. Verify Setup

After creating your `.env` file, start the application:

```bash
mvn spring-boot:run
```

You should see this message in the console:
```
✓ Environment variables loaded from .env file
```

## Security Best Practices

### ✅ DO:
- Keep `.env` file in `.gitignore` (already configured)
- Use strong, unique passwords
- Generate a secure JWT secret
- Use Gmail App Passwords, not your regular password
- Share `.env.example` with your team
- Update `.env` values for different environments (dev, staging, prod)

### ❌ DON'T:
- Never commit `.env` file to Git
- Never share your `.env` file publicly
- Never use default/weak passwords in production
- Never hardcode sensitive data in source code

## Environment Variables in application.properties

The `application.properties` file now uses environment variables with fallback defaults:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/go_delivery_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:password}
```

Format: `${ENV_VAR:default_value}`
- If `ENV_VAR` exists in `.env`, it will be used
- Otherwise, `default_value` will be used

## Troubleshooting

### .env file not loading
1. Make sure `.env` file is in the project root (same level as `pom.xml`)
2. Check file name is exactly `.env` (not `.env.txt`)
3. Verify `DotenvConfig.java` is in the correct package
4. Check `spring.factories` file exists in `src/main/resources/META-INF/`

### Database connection failed
1. Verify `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` are correct
2. Ensure PostgreSQL is running
3. Check database exists: `go_delivery_db`

### Email not sending
1. Verify Gmail credentials are correct
2. Use App Password, not regular password
3. Enable "Less secure app access" if needed (not recommended)
4. Check Gmail App Passwords: https://myaccount.google.com/apppasswords

### JWT errors
1. Ensure `JWT_SECRET` is at least 256 bits (32 characters in hex)
2. Generate new secret: `openssl rand -hex 32`

## Different Environments

### Development
Use `.env` file (already configured)

### Production
Set environment variables directly on the server:

**Linux/Mac:**
```bash
export DB_URL="jdbc:postgresql://prod-host:5432/go_delivery_db"
export DB_USERNAME="prod_user"
export DB_PASSWORD="secure_password"
# ... other variables
```

**Windows:**
```cmd
set DB_URL=jdbc:postgresql://prod-host:5432/go_delivery_db
set DB_USERNAME=prod_user
set DB_PASSWORD=secure_password
```

**Docker:**
```yaml
environment:
  - DB_URL=jdbc:postgresql://db:5432/go_delivery_db
  - DB_USERNAME=postgres
  - DB_PASSWORD=secure_password
```

**Cloud Platforms (Heroku, AWS, Azure):**
Use their environment variable configuration UI

## Files Structure

```
goDelivery/
├── .env                    # Your actual secrets (NEVER commit)
├── .env.example            # Template file (commit this)
├── .gitignore              # Includes .env
├── pom.xml
├── ENV_SETUP.md            # This file
└── src/
    └── main/
        ├── java/
        │   └── com/goDelivery/goDelivery/
        │       └── config/
        │           └── DotenvConfig.java
        └── resources/
            ├── application.properties
            └── META-INF/
                └── spring.factories
```

## Support

If you encounter any issues with environment setup, please:
1. Check this documentation
2. Verify your `.env` file format
3. Check application logs for error messages
4. Contact the development team

---

**Remember**: Never commit sensitive data to version control! 🔒
