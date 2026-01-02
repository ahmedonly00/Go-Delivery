# Branch Order and Payment Management

## Overview
Branch managers can fully manage orders and payments for their branch, including viewing orders, updating status, processing payments, and handling refunds. The system ensures proper access control so branch managers can only access their own branch's data.

## Order Management Features

### 1. **View Orders**
- Get all orders for the branch
- Filter by status (pending, preparing, ready, completed)
- Real-time order updates

### 2. **Order Status Updates**
- Accept pending orders
- Mark orders as preparing
- Mark orders ready for pickup/delivery
- Complete orders
- Cancel orders with reason

### 3. **Order Workflow**
```
PENDING → PREPARING → READY → COMPLETED
    ↓         ↓         ↓
  CANCEL   CANCEL   CANCEL
```

## Payment Management Features

### 1. **View Payments**
- View all payments for branch orders
- Filter by payment status
- View payment details

### 2. **Payment Operations**
- Process refunds
- View payment history
- Track failed payments

## API Endpoints

### Order Management

#### Get Branch Orders
```
GET /api/v1/branch-orders/branch/{branchId}
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

#### Get Pending Orders
```
GET /api/v1/branch-orders/branch/{branchId}/pending
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

#### Update Order Status
```
PUT /api/v1/branch-orders/branch/{branchId}/update-status/{orderId}
Body: {
  "status": "PREPARING|READY|COMPLETED|CANCELLED",
  "comments": "Optional comment"
}
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

#### Accept Order
```
POST /api/v1/branch-orders/branch/{branchId}/accept-order/{orderId}
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

#### Mark Order Ready
```
POST /api/v1/branch-orders/branch/{branchId}/ready-for-pickup/{orderId}
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

#### Complete Order
```
POST /api/v1/branch-orders/branch/{branchId}/complete-order/{orderId}
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

#### Cancel Order
```
POST /api/v1/branch-orders/branch/{branchId}/cancel-order/{orderId}?cancellationReason=reason
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

### Payment Management

#### Get Branch Payments
```
GET /api/v1/branch-orders/branch/{branchId}/payments
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

#### Get Payment Details
```
GET /api/v1/branch-orders/branch/{branchId}/payment/{paymentId}
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

#### Process Refund
```
POST /api/v1/branch-orders/branch/{branchId}/refund/{paymentId}?refundReason=reason
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

### Statistics

#### Get Branch Statistics
```
GET /api/v1/branch-orders/branch/{branchId}/stats
Response: {
  "totalOrders": 150,
  "completedOrders": 120,
  "cancelledOrders": 10,
  "pendingOrders": 20
}
Roles: RESTAURANT_ADMIN, BRANCH_MANAGER
```

## Security and Access Control

### Authentication
- All endpoints require authentication
- JWT tokens identify the user and their role

### Authorization
- **Branch Managers**: Can only access their own branch's orders and payments
- **Restaurant Admins**: Can access all branches' orders and payments

### Verification Process
1. Extract username from authentication token
2. Check if user is restaurant admin or branch manager
3. For branch managers: Verify they belong to the requested branch
4. For restaurant admins: Verify they own the restaurant that owns the branch
5. Grant or deny access based on verification

## Order Status Flow

### 1. **Order Created**
- Status: `PENDING`
- Branch receives notification
- Branch can accept or reject

### 2. **Order Accepted**
- Status: `PREPARING`
- Branch starts preparing the order
- Customer receives preparation notification

### 3. **Order Ready**
- Status: `READY`
- Order is ready for pickup/delivery
- Customer receives ready notification

### 4. **Order Completed**
- Status: `COMPLETED`
- Order delivered/picked up
- Payment processed

### 5. **Order Cancelled**
- Status: `CANCELLED`
- Order cancelled by branch or customer
- Refund processed if payment made

## Payment Processing

### Payment Methods Supported
- Mobile Money (M-Pesa, MoMo)
- Cash on delivery
- Card payments (via integration)

### Payment Workflow
1. Customer places order
2. Payment initiated
3. Payment processed/confirmed
4. Order status updated
5. Branch notified of payment

### Refund Process
1. Branch initiates refund
2. Refund reason recorded
3. Payment gateway processes refund
4. Customer notified of refund
5. Order status updated to refunded

## Real-time Features

### Order Notifications
- New order alerts
- Status change notifications
- Payment confirmations

### Dashboard Integration
- Live order count
- Revenue tracking
- Popular items
- Peak hours analysis

## Best Practices

### For Branch Managers
1. **Accept orders promptly** - Reduces customer wait time
2. **Update status regularly** - Keeps customers informed
3. **Handle cancellations professionally** - Maintain good ratings
4. **Verify payments before delivery** - Avoid fraud
5. **Keep inventory updated** - Prevent out-of-stock issues

### For Restaurant Admins
1. **Monitor performance** - Track branch efficiency
2. **Set clear policies** - Standardize operations
3. **Provide training** - Ensure proper system usage
4. **Regular audits** - Verify financial accuracy

## Integration Points

### Kitchen Display System (KDS)
- Automatic order display
- Timer tracking
- Status updates

### Delivery Partners
- Order forwarding
- Real-time tracking
- Delivery confirmation

### Inventory System
- Automatic stock deduction
- Low stock alerts
- Purchase order generation

## Troubleshooting

### Common Issues
1. **Order not visible**: Check branch assignment and permissions
2. **Status update fails**: Verify order state transition validity
3. **Payment not showing**: Check payment gateway integration
4. **Refund errors**: Verify payment status and time limits

### Error Handling
- Proper error messages for invalid operations
- Fallback mechanisms for payment failures
- Audit trail for all operations

## Future Enhancements

1. **AI-powered order prediction**
2. **Automated routing for delivery**
3. **Customer sentiment analysis**
4. **Dynamic pricing based on demand**
5. **Multi-branch order splitting**
