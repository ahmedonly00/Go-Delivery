# MoMo Payment API — Frontend Integration Guide

> **Updated:** 2026-02-20  
> **Base URL:** `https://delivery.apis.ivas.rw:8085`

---

## Overview

The payment flow now uses a **single API call** that handles both collection from the customer and distribution to all involved restaurants automatically. You no longer need to manage disbursements separately.

---

## Initiate Payment

### `POST /api/v1/payments/momo/request`

Triggers a MoMo USSD pop-up on the customer's phone to collect payment. Once the customer approves, MoMo automatically disburses funds to the restaurant(s).

### Request Headers

```http
Content-Type: application/json
Authorization: Bearer <token>
```

### Request Body

```json
{
  "orderId": 123
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `orderId` | `Long` | ✅ Yes | The ID of the order to pay for |
| `msisdn` | `String` | ❌ Optional | Override phone number for payment (e.g. `"250788999999"`). If omitted, the pop-up is sent to the customer's registered number |

> **Note on `msisdn`:** If the customer wants to pay from a different MoMo wallet (not the one they registered with), pass the target phone number here in the format `250XXXXXXXXX`.

---

### Success Response — `200 OK`

```json
{
  "referenceId": "880b1700-h51e-74g7-d049-779988773333",
  "status": "PENDING",
  "message": "Collection initiated, disbursements will be processed when collection succeeds",
  "amount": 2000.0,
  "currency": "RWF",
  "externalId": "COLL_ORD-20260220-1234"
}
```

| Field | Description |
|-------|-------------|
| `referenceId` | Use this to poll for payment status |
| `status` | Always `PENDING` on initiation |
| `amount` | Total amount being collected from the customer |
| `externalId` | Internal reference for this collection |

### Error Responses

| Status | Reason |
|--------|--------|
| `400` | Order is already paid, or order cannot be found |
| `404` | Order ID does not exist |
| `500` | MoMo API or server error |

---

## Check Payment Status

### `GET /api/v1/payments/momo/status/{referenceId}`

Poll this endpoint to check if the customer's payment has been confirmed.

```http
GET /api/v1/payments/momo/status/880b1700-h51e-74g7-d049-779988773333
Authorization: Bearer <token>
```

### Response

```json
{
  "referenceId": "880b1700-h51e-74g7-d049-779988773333",
  "status": "SUCCESSFUL",
  "amount": 2000.0,
  "externalId": "COLL_ORD-20260220-1234"
}
```

**Status values:**

| Status | Meaning |
|--------|---------|
| `PENDING` | Waiting for customer to approve on their phone |
| `SUCCESSFUL` | Payment confirmed — order is now being processed |
| `FAILED` | Customer declined or payment timed out |
| `CANCELLED` | Payment was cancelled |

---

## Recommended Frontend Flow

```
1. Customer taps "Pay with MoMo"
   └─ Optionally show a field: "Pay from a different number?" (optional msisdn)

2. POST /api/v1/payments/momo/request
   Body: { "orderId": 123 }       ← or { "orderId": 123, "msisdn": "250788..." }
   └─ Save the returned referenceId

3. Show "Check your phone" screen
   └─ Customer approves the USSD pop-up on their phone

4. Poll GET /api/v1/payments/momo/status/{referenceId}
   every 10–15 seconds until status != "PENDING"

5. On SUCCESSFUL → navigate to order tracking screen
   On FAILED / CANCELLED → show error and allow retry
```

---

## What Changed vs. Previous Implementation

| | Before | After |
|--|--------|-------|
| **Request body** | Required `externalId`, `msisdn`, `amount`, `payerMessageTitle`, `payerMessageDescription`, `callback` | Only `orderId` required |
| **API calls** | Two calls: collection first, then disbursement | One call handles everything |
| **Disbursement** | Had to be triggered separately | Automatic — MoMo handles it internally |
| **Endpoint URL** | `/api/v1/payments/momo/request` | `/api/v1/payments/momo/request` *(unchanged)* |
| **Response shape** | `MomoPaymentResponse` | `CollectionDisbursementResponse` (see fields above) |

---

## Example — JavaScript / TypeScript

```typescript
// Initiate payment (registered number)
const initiatePayment = async (orderId: number) => {
  const res = await fetch(`${BASE_URL}/api/v1/payments/momo/request`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify({ orderId }),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json(); // { referenceId, status, amount, ... }
};

// Initiate payment with a different number
const initiatePaymentWithNumber = async (orderId: number, msisdn: string) => {
  const res = await fetch(`${BASE_URL}/api/v1/payments/momo/request`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify({ orderId, msisdn }),
  });

  if (!res.ok) throw new Error(await res.text());
  return res.json();
};

// Poll for payment status
const pollPaymentStatus = async (referenceId: string): Promise<string> => {
  while (true) {
    const res = await fetch(
      `${BASE_URL}/api/v1/payments/momo/status/${referenceId}`,
      { headers: { 'Authorization': `Bearer ${token}` } }
    );

    const data = await res.json();

    if (data.status !== 'PENDING') return data.status; // SUCCESSFUL | FAILED | CANCELLED

    await new Promise(r => setTimeout(r, 10000)); // wait 10 seconds before next poll
  }
};
```

---

## Example — Flutter / Dart

```dart
// Initiate payment
Future<Map<String, dynamic>> initiatePayment(int orderId, {String? msisdn}) async {
  final body = <String, dynamic>{'orderId': orderId};
  if (msisdn != null) body['msisdn'] = msisdn;

  final response = await http.post(
    Uri.parse('$baseUrl/api/v1/payments/momo/request'),
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    },
    body: jsonEncode(body),
  );

  if (response.statusCode != 200) throw Exception(response.body);
  return jsonDecode(response.body);
}

// Poll for payment status
Future<String> pollPaymentStatus(String referenceId) async {
  while (true) {
    final response = await http.get(
      Uri.parse('$baseUrl/api/v1/payments/momo/status/$referenceId'),
      headers: {'Authorization': 'Bearer $token'},
    );

    final data = jsonDecode(response.body);
    if (data['status'] != 'PENDING') return data['status'];

    await Future.delayed(const Duration(seconds: 10));
  }
}
```
