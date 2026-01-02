# Automatic Disbursement System

## Overview
The system automatically disburses funds to restaurants for ALL paid orders using the collection-disbursement mechanism.

## How It Works

### 1. Customer Payment Flow
```
Customer places order → Customer pays via MoMo → Payment confirmed → Automatic disbursement triggered
```

### 2. Single Restaurant Order
- **Customer pays**: $30 for pizza
- **Platform commission**: $4.50 (15%)
- **Restaurant receives**: $25.50
- **Disbursement**: Automatic via collection-disbursement API

### 3. Multi-Restaurant Order
- **Customer pays**: $50 total (Pizza $30 + Burger $20)
- **Platform commissions**: $4.50 + $3.00 = $7.50
- **Restaurant A receives**: $25.50
- **Restaurant B receives**: $17.00
- **Disbursements**: Automatic to both restaurants

## Configuration

### Enable/Disable Auto-Disbursement
```properties
# Enable for all orders
app.payment.auto-disbursement.enabled=true

# Disable completely
app.payment.auto-disbursement.enabled=false
```

### Commission Settings
```properties
# Default commission rate (15%)
app.commission.default-rate=0.15

# Minimum commission per transaction
app.commission.minimum-amount=0.0

# Maximum commission per transaction
app.commission.maximum-amount=1000.00
```

## API Flow

### Collection-Disbursement Request
```json
{
  "collectionExternalId": "COLL_ORDER123",
  "collectionMsisdn": "250788111111",
  "collectionAmount": 50.00,
  "disbursementRecipients": [
    {
      "externalId": "DISP_ORDER123_01",
      "msisdn": "250788123456",
      "amount": 25.50
    }
  ]
}
```

### Response
```json
{
  "referenceId": "REF123456",
  "status": "PENDING",
  "message": "Collection initiated"
}
```

## Monitoring

### Status Polling
- System polls every 30 seconds
- Tracks collection status
- Updates individual disbursement statuses

### Webhook Callbacks
- Collection completion notification
- Individual disbursement status updates
- Error handling for failed transactions

## Benefits

1. **Consistent Process**: Same mechanism for all orders
2. **Automatic**: No manual intervention needed
3. **Transparent**: Clear tracking of all transactions
4. **Configurable**: Easy to enable/disable or adjust rates
5. **Scalable**: Works for any number of restaurants

## Manual Override

If auto-disbursement fails, you can manually trigger:
```http
POST /api/v1/payments/orders/{orderId}/disburse
```

## Reporting

Track all disbursements via:
- GET `/api/v1/disbursements/restaurant/summary` - Restaurant view
- GET `/api/v1/disbursements/admin/summary` - Admin view
