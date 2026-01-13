#!/bin/bash

echo "=== Moving files to correct location ==="

# Move menu uploads
sudo mv /var/www/delivery-backend/Go-Delivery/uploads/restaurants/*/menu-uploads /var/www/delivery-backend/uploads/restaurants/ 2>/dev/null || true

# Move any other restaurant files
sudo find /var/www/delivery-backend/Go-Delivery/uploads/restaurants -type f -exec mv {} /var/www/delivery-backend/uploads/restaurants/ \; 2>/dev/null || true

# Move menu-items if any exist
sudo mv /var/www/delivery-backend/Go-Delivery/uploads/menu-items/* /var/www/delivery-backend/uploads/menu-items/ 2>/dev/null || true

# Remove the old directory
sudo rm -rf /var/www/delivery-backend/Go-Delivery/uploads

# Set correct permissions
sudo chown -R www-data:www-data /var/www/delivery-backend/uploads
sudo chmod -R 755 /var/www/delivery-backend/uploads

echo "=== Verifying the move ==="
echo "Files in correct location:"
find /var/www/delivery-backend/uploads -type f | grep -E "\.(jpg|jpeg|png|pdf)$" | head -10

echo -e "\nChecking if old directory still exists:"
ls -la /var/www/delivery-backend/Go-Delivery/uploads 2>/dev/null || echo "Old directory successfully removed"
