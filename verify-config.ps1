# Configuration Verification Script
# Run this to check if your .env file is properly configured

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "   Go Delivery - Configuration Verification" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

$envFile = ".env"
$issuesFound = $false

# Check if .env file exists
if (-Not (Test-Path $envFile)) {
    Write-Host "❌ ERROR: .env file not found!" -ForegroundColor Red
    Write-Host "   Expected location: $(Get-Location)\$envFile" -ForegroundColor Yellow
    Write-Host "   Please copy .env.example to .env and configure it" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ .env file found" -ForegroundColor Green
Write-Host ""

# Read .env file
$envContent = Get-Content $envFile

# Required variables
$requiredVars = @(
    "DB_URL",
    "DB_USERNAME", 
    "DB_PASSWORD",
    "MAIL_USERNAME",
    "MAIL_PASSWORD",
    "JWT_SECRET",
    "JWT_EXPIRATION",
    "APP_BASE_URL",
    "FRONTEND_URL",
    "SERVER_PORT",
    "SUPER_ADMIN_EMAIL",
    "SUPER_ADMIN_PASSWORD"
)

Write-Host "Checking required environment variables..." -ForegroundColor Cyan
Write-Host ""

foreach ($var in $requiredVars) {
    $found = $false
    $value = ""
    
    foreach ($line in $envContent) {
        if ($line -match "^\s*$var\s*=\s*(.*)$") {
            $found = $true
            $value = $Matches[1].Trim()
            break
        }
    }
    
    if ($found) {
        # Check if it's a placeholder value
        $isPlaceholder = $false
        $placeholders = @(
            "your_database_password",
            "your-email@gmail.com",
            "your-gmail-app-password",
            "your-secret-key",
            "your-app-password"
        )
        
        foreach ($placeholder in $placeholders) {
            if ($value -like "*$placeholder*") {
                $isPlaceholder = $true
                break
            }
        }
        
        if ($isPlaceholder) {
            Write-Host "⚠️  $var = $value" -ForegroundColor Yellow
            Write-Host "   ^ This looks like a placeholder. Please update with actual value!" -ForegroundColor Yellow
            $issuesFound = $true
        } elseif ($value -eq "") {
            Write-Host "❌ $var is empty!" -ForegroundColor Red
            $issuesFound = $true
        } else {
            # Mask sensitive values
            if ($var -like "*PASSWORD*" -or $var -like "*SECRET*") {
                $maskedValue = "*" * $value.Length
                Write-Host "✅ $var = $maskedValue" -ForegroundColor Green
            } else {
                Write-Host "✅ $var = $value" -ForegroundColor Green
            }
        }
    } else {
        Write-Host "❌ $var is missing!" -ForegroundColor Red
        $issuesFound = $true
    }
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan

# Check for common formatting issues
Write-Host "Checking for common formatting issues..." -ForegroundColor Cyan
Write-Host ""

$lineNumber = 0
foreach ($line in $envContent) {
    $lineNumber++
    
    # Skip comments and empty lines
    if ($line -match "^\s*#" -or $line -match "^\s*$") {
        continue
    }
    
    # Check for spaces around =
    if ($line -match "^\s*\w+\s+=\s*" -or $line -match "^\s*\w+=\s+") {
        Write-Host "⚠️  Line $lineNumber has spaces around '='" -ForegroundColor Yellow
        Write-Host "   $line" -ForegroundColor Gray
        $issuesFound = $true
    }
    
    # Check for quotes
    if ($line -match '^\s*\w+=["\'].*["\']') {
        Write-Host "⚠️  Line $lineNumber has quotes around value (usually not needed)" -ForegroundColor Yellow
        Write-Host "   $line" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan

# Test PostgreSQL connection
Write-Host "Testing PostgreSQL connection..." -ForegroundColor Cyan
Write-Host ""

$psqlPath = "C:\Program Files\PostgreSQL\16\bin\psql.exe"
if (Test-Path $psqlPath) {
    Write-Host "✅ PostgreSQL client found" -ForegroundColor Green
    Write-Host "   You can test connection with:" -ForegroundColor Gray
    Write-Host "   & '$psqlPath' -U postgres -d go_delivery_db" -ForegroundColor Gray
} else {
    Write-Host "⚠️  PostgreSQL client not found at default location" -ForegroundColor Yellow
    Write-Host "   Make sure PostgreSQL is installed" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan

# Summary
Write-Host ""
if ($issuesFound) {
    Write-Host "❌ ISSUES FOUND - Please fix the issues above" -ForegroundColor Red
    Write-Host ""
    Write-Host "Common fixes:" -ForegroundColor Yellow
    Write-Host "1. Replace placeholder values with actual credentials" -ForegroundColor Yellow
    Write-Host "2. Remove spaces around '=' signs" -ForegroundColor Yellow
    Write-Host "3. Remove quotes around values" -ForegroundColor Yellow
    Write-Host "4. Generate Gmail App Password for MAIL_PASSWORD" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "See CONFIG_VERIFICATION.md for detailed instructions" -ForegroundColor Cyan
} else {
    Write-Host "✅ Configuration looks good!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. Make sure PostgreSQL is running" -ForegroundColor White
    Write-Host "2. Start the application" -ForegroundColor White
    Write-Host "3. Check startup logs for email configuration" -ForegroundColor White
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
