$env:DB_USERNAME = 'mozuser'
$env:DB_PASSWORD = 'mozuser@2018'
# URL-encode the @ symbol in the password for JDBC URL
$env:DB_URL = 'jdbc:postgresql://localhost:5432/moz_delivery_db?useSSL=false'

Write-Host "Starting backend with database connection to localhost:5432..."
Write-Host "Database: moz_delivery_db"
Write-Host "User: mozuser"
Write-Host ""

java -jar target/goDelivery-0.0.1-SNAPSHOT.jar 2>&1 | Tee-Object -FilePath backend-startup.log
