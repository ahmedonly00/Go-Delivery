# GoDelivery Payment Flow Testing Guide

This guide provides comprehensive JSON examples and steps to test the complete payment flow in GoDelivery.

## Prerequisites

1. **Authentication**: You'll need a JWT token. Replace `YOUR_JWT_TOKEN_HERE` in the requests with a valid token.
2. **Base URL**: Default is `http://localhost:8080`. Update if your server runs on a different port/host.
3. **Test Data**: Ensure you have test data for:
   - Customer (ID: 1)
   - Restaurant (ID: 1) with Branch (ID: 1)
   - Menu Items (IDs: 1, 2)

## Payment Flow Steps

### 1. Create an Order

**Endpoint**: `POST /api/orders/createOrder`

```json
{
  "customerId": 1,
  "deliveryAddressId": 1,
  "orderStatus": "PENDING",
  "deliveryAddress": "123 Main Street, Maputo, Mozambique",
  "paymentStatus": "UNPAID",
  "subTotal": 250.00,
  "deliveryFee": 20.00,
  "discountAmount": 10.00,
  "finalAmount": 260.00,
  "paymentMethod": "MOMO",
  "specialInstructions": "Please deliver to the gate",
  "restaurantOrders": [
    {
      "restaurantId": 1,
      "branchId": 1,
      "orderItems": [
        {
          "menuItemId": 1,
          "quantity": 2,
          "specialInstructions": "Extra spicy"
        },
        {
          "menuItemId": 2,
          "quantity": 1,
          "specialInstructions": "No onions"
        }
      ]
    }
  ]
}
```

**Expected Response**: Array of created orders with their IDs. Save the first order's ID for the next steps.

### 2. Initiate MoMo Payment

**Endpoint**: `POST /api/v1/payments/momo/request`

```json
{
  "externalId": "ORDER_123_PAYMENT",
  "msisdn": "250841234567",
  "amount": 260.00,
  "payerMessageTitle": "GoDelivery Order Payment",
  "payerMessageDescription": "Payment for order #123-456",
  "callback": "https://delivery.apis.ivas.rw:8085/api/v1/payments/momo/webhook"
}
```

**Important Notes**:
- `msisdn` must be in format: `250XXXXXXXXX` (Mozambique country code)
- `amount` should match the order's final amount
- `externalId` should be unique per payment attempt

**Expected Response**:
```json
{
  "referenceId": "MOMO_REF_123456",
  "status": "PENDING",
  "message": "Payment initiated successfully"
}
```

Save the `referenceId` for status checking.

### 3. Check Payment Status

**Endpoint**: `GET /api/v1/payments/momo/status/{referenceId}`

No request body needed. Use the referenceId from step 2.

**Expected Response**:
```json
{
  "referenceId": "MOMO_REF_123456",
  "status": "SUCCESSFUL",
  "amount": 260.00,
  "currency": "MZN"
}
```

Possible statuses: `PENDING`, `SUCCESSFUL`, `FAILED`

### 4. Process Payment (After Confirmation)

Once the payment is confirmed as successful, process it in the system:

**Endpoint**: `POST /api/payments/process`

```json
{
  "orderId": 123,
  "paymentMethod": "MOMO",
  "paymentProvider": "MOMO_MZ",
  "phoneNumber": "250841234567",
  "transactionId": "TXN_123456789",
  "referenceNumber": "REF_987654321",
  "amount": 260.00,
  "currency": "MZN",
  "gatewayResponse": "{\"status\":\"SUCCESSFUL\",\"transactionId\":\"TXN_123456789\"}"
}
```

**Payment Methods Available**:
- `CASH`
- `MPESA`
- `EMOLA`
- `MOMO`
- `CARD`

### 5. Get Payment Details

**Endpoint**: `GET /api/payments/getPaymentByOrderId/{orderId}`

No request body needed.

**Expected Response**:
```json
{
  "paymentId": 456,
  "orderId": 123,
  "paymentMethod": "MOMO",
  "paymentStatus": "PAID",
  "amount": 260.00,
  "currency": "MZN",
  "transactionId": "TXN_123456789",
  "createdAt": "2024-01-04T12:00:00"
}
```

### 6. Process Order Disbursement (To Restaurant)

After payment is processed, disburse funds to the restaurant:

**Endpoint**: `POST /api/v1/payments/momo/processOrderDisbursement`

```json
{
  "orderId": 123,
  "orderNumber": "ORD-2024-001",
  "paymentStatus": "PAID",
  "finalAmount": 260.00,
  "restaurant": {
    "restaurantId": 1,
    "restaurantName": "Test Restaurant"
  }
}
```

**Expected Response**:
```json
{
  "referenceId": "DISB_789012",
  "status": "PENDING",
  "amount": 234.00,
  "commission": 26.00,
  "message": "Disbursement initiated successfully"
}
```

### 7. Check Disbursement Status

**Endpoint**: `GET /api/v1/payments/momo/disbursement/status/{disbursementReferenceId}`

No request body needed.

**Expected Response**:
```json
{
  "referenceId": "DISB_789012",
  "status": "SUCCESSFUL",
  "amount": 234.00,
  "restaurantName": "Test Restaurant",
  "processedAt": "2024-01-04T12:05:00"
}
```

## Testing with Postman

1. Import the collection file `GoDelivery-Payment-Flow.postman_collection.json` into Postman
2. Set the environment variables:
   - `baseUrl`: Your server URL (default: http://localhost:8080)
   - `JWT_TOKEN`: Your authentication token
3. Execute requests in sequence (1 through 7)
4. The collection includes scripts to automatically save IDs between requests

## Alternative Payment Methods

### For M-PESA Payments

Use the M-PESA controller endpoints:
- Initiate: `POST /api/payments/mpesa/request`
- Status: `GET /api/payments/mpesa/status/{referenceId}`

### For Cash Payments

Skip steps 2-3 and directly process payment with:
```json
{
  "orderId": 123,
  "paymentMethod": "CASH",
  "paymentProvider": "CASH_ON_DELIVERY",
  "phoneNumber": "250841234567",
  "transactionId": "CASH_123456",
  "referenceNumber": "CASH_REF_123",
  "amount": 260.00,
  "currency": "MZN",
  "gatewayResponse": "{\"status\":\"COLLECTED_ON_DELIVERY\"}"
}
```

## Error Handling

Common error responses:

### Invalid Phone Number
```json
{
  "status": 400,
  "error": "Invalid phone number format. Must be 250XXXXXXXXX"
}
```

### Order Not Found
```json
{
  "status": 404,
  "error": "Order not found with id: 123"
}
```

### Payment Already Processed
```json
{
  "status": 400,
  "error": "Order is already paid"
}
```

## Webhooks

The system uses webhooks for payment notifications:
- MoMo Payment Webhook: `POST /api/v1/payments/momo/webhook`
- MoMo Disbursement Webhook: `POST /api/v1/payments/momo/disbursement`

Ensure your webhook URLs are publicly accessible for production use.

## Tips for Testing

1. Use unique `externalId` values for each payment attempt
2. Ensure the order's `paymentStatus` is `UNPAID` before initiating payment
3. Wait for payment to be `SUCCESSFUL` before processing it
4. Test with different payment methods
5. Verify the order status updates to `PAID` after successful payment
6. Check that disbursement only works after payment is processed

## Troubleshooting

- **Payment initiation fails**: Check phone number format and amount
- **Status check returns 404**: Verify the referenceId is correct
- **Payment processing fails**: Ensure the payment was successful in the payment provider system
- **Disbursement fails**: Check that the order is paid and the restaurant has valid disbursement details
