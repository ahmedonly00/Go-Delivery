#!/bin/bash

SERVER="ahmed@188.166.16.179"
APP_DIR="/var/www/delivery-backend"
BUILD_DIR="Go-Delivery"  # Adjust if different
SERVICE_NAME="delivery-backend"

echo "🚀 Starting Backend Deployment..."
echo "================================"

# Check for uncommitted changes
if [[ -n $(git status -s) ]]; then
    echo "⚠️  You have uncommitted changes:"
    git status -s
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Deployment cancelled"
        exit 1
    fi
fi

# Commit and push
echo "📤 Committing and pushing to Git..."
git add .
git commit -m "Deploy: $(date +%Y-%m-%d_%H:%M:%S)" || true
git push origin main

if [ $? -ne 0 ]; then
    echo "❌ Git push failed!"
    exit 1
fi

# Deploy on server
echo "🚀 Deploying on server..."
ssh $SERVER << 'ENDSSH'
set -e

echo "📥 Pulling latest code..."
cd /var/www/delivery-backend
git pull origin main

echo "🔨 Building application..."
cd Go-Delivery
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed on server!"
    exit 1
fi

echo "📦 JAR built successfully"
ls -lh target/*.jar

echo "🔄 Restarting service..."
sudo systemctl restart delivery-backend

echo "⏳ Waiting for service to start..."
sleep 10

echo "✅ Checking service status..."
sudo systemctl status delivery-backend --no-pager

echo "🔍 Testing backend..."
if curl -s http://localhost:8085/api/health > /dev/null; then
    echo "✅ Backend is responding!"
else
    echo "⚠️  Backend might not be responding. Check logs."
fi

ENDSSH

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Deployment Complete!"
    echo "========================"
    echo "🌐 Backend: https://delivery.apis.ivas.rw"
    echo ""
    echo "📊 View logs:"
    echo "   ssh $SERVER 'sudo journalctl -u delivery-backend -f'"
else
    echo ""
    echo "❌ Deployment failed! Check errors above."
    exit 1
fi