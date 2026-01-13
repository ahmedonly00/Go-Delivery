#!/bin/bash

echo "=== Checking upload directories ==="
echo "1. Configured in application.properties:"
grep "file.upload-dir" src/main/resources/application.properties

echo -e "\n2. Actual files in /var/www/delivery-backend/uploads:"
find /var/www/delivery-backend/uploads -type f 2>/dev/null | head -10

echo -e "\n3. Files in Go-Delivery/uploads:"
find /var/www/delivery-backend/Go-Delivery/uploads -type f 2>/dev/null | head -10

echo -e "\n4. Current directory permissions:"
ls -la /var/www/delivery-backend/
ls -la /var/www/delivery-backend/uploads/

echo -e "\n5. Application process details:"
ps aux | grep java | grep goDelivery
