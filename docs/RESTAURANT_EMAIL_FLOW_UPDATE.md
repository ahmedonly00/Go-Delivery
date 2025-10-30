# Restaurant Email Flow Update

## Overview
Updated the email notification flow for restaurant registration. Instead of sending welcome and OTP emails, restaurant admins now receive an "under review" notification after completing their setup.

---

## Previous Flow (COMMENTED OUT)

### ❌ Old Behavior:

1. **Admin Registration** → Send Welcome Email
2. **Restaurant Setup Complete** → Send OTP Email for verification
3. **OTP Verification** → Account activated

### Issues with Old Flow:
- OTP verification not needed since Super Admin reviews documents
- Welcome email sent too early (before restaurant is ready)
- Confusing for restaurant admins

---

## New Flow (CURRENT)

### ✅ New Behavior:

1. **Admin Registration** → No email sent
2. **Restaurant Setup Complete (Menu Uploaded)** → Send "Under Review" Email
3. **Super Admin Reviews Documents** → Approve or Reject
4. **If Approved** → Send Approval Email (restaurant goes live)
5. **If Rejected** → Send Rejection Email with reason

---

## Email Timeline

```
Registration
    ↓
    (No Email)
    ↓
Complete Setup + Upload Menu
    ↓
    📧 "Under Review" Email
    ↓
    ⏳ Wait for Super Admin Review (up to 24 hours)
    ↓
    ┌─────────────────┬─────────────────┐
    │   APPROVED      │    REJECTED     │
    ↓                 ↓                 ↓
📧 Approval Email   📧 Rejection Email
   (Go Live!)          (With Reason)
```

---

## Code Changes

### 1. EmailService - New Method

**File:** `EmailService.java`

**Added:**
```java
@Async
public void sendRestaurantUnderReviewEmail(String toEmail, String ownerName, String restaurantName) {
    // Sends email notifying restaurant admin that application is under review
    // Expected response time: 24 hours
}
```

---

### 2. RestaurantRegistrationService - Commented Out Welcome Email

**File:** `RestaurantRegistrationService.java`

**Method:** `registerRestaurantAdmin()`

**Before:**
```java
admin = userRepository.save(admin);
emailService.sendWelcomeEmail(admin.getEmail(), admin.getFullName(), "");
return userMapper.toAdminResponseDTO(admin);
```

**After:**
```java
admin = userRepository.save(admin);

// COMMENTED OUT: Welcome email will not be sent during registration
// Instead, "under review" email will be sent after restaurant setup completion
// try {
//     emailService.sendWelcomeEmail(admin.getEmail(), admin.getFullName(), "");
//     log.info("Welcome email sent to new restaurant admin: {}", admin.getEmail());
// } catch (Exception e) {
//     log.error("Failed to send welcome email: {}", e.getMessage());
// }

return userMapper.toAdminResponseDTO(admin);
```

---

### 3. RestaurantRegistrationService - Send Under Review Email

**File:** `RestaurantRegistrationService.java`

**Method:** `completeRestaurantRegistration()`

**Added:**
```java
// Save the updated admin and restaurant
userRepository.save(admin);
restaurantRepository.save(savedRestaurant);

// Send "under review" email instead of OTP
try {
    emailService.sendRestaurantUnderReviewEmail(
        admin.getEmail(), 
        admin.getFullName(), 
        savedRestaurant.getRestaurantName()
    );
    log.info("Under review email sent to restaurant admin: {}", admin.getEmail());
} catch (Exception e) {
    log.error("Failed to send under review email: {}", e.getMessage());
}
```

---

## Email Template

### "Under Review" Email

**Template:** `restaurant-under-review-email.html`

**Subject:** "Your Restaurant is Under Review - [Restaurant Name]"

**Content:**
- ✅ Application submitted successfully
- ✅ Documents are being reviewed
- ✅ Expected response time: 24 hours
- ✅ Timeline of what happens next
- ✅ List of documents being reviewed
- ✅ Support contact information

**Key Features:**
- Professional and reassuring tone
- Clear timeline expectations
- Visual timeline showing current stage
- Contact information for support

---

## Email Content Preview

```
Subject: Your Restaurant is Under Review - Pizza Palace

Dear John Doe,

We have successfully received your restaurant application for Pizza Palace. 
Thank you for choosing MozFood as your delivery partner!

🔍 Your Application is Under Review

Our team is currently reviewing your submitted documents and restaurant information.
This process typically takes up to 24 hours.

What Happens Next?

✓ Application Submitted
  Your restaurant details and documents have been received

⏳ Document Review (Current Stage)
  Our team is verifying your business documents and information

📧 Decision Notification
  You'll receive an email with our decision within 24 hours

🎉 Go Live (If Approved)
  Your restaurant will be visible to customers on MozFood

⏰ Expected Response Time: Within 24 Hours

What We're Reviewing:
✓ Commercial Registration Certificate
✓ Tax Identification Number (NUIT)
✓ Business Operating License
✓ Restaurant Information & Menu
```

---

## OTP Email Status

### ❌ OTP Email (COMMENTED OUT)

The OTP email functionality is **not deleted** but is **commented out** in the flow. It can be re-enabled if needed in the future.

**Location:** `RestaurantRegistrationService.java`

**Why Commented Out:**
- Restaurant admins don't need email verification via OTP
- Super Admin reviews and approves the restaurant
- Approval email serves as confirmation
- Simplifies the registration flow

---

## Testing

### Test Scenario 1: New Restaurant Registration

**Steps:**
1. Register as restaurant admin
2. Complete restaurant setup (upload menu)
3. Check email inbox

**Expected:**
- ✅ No email after registration
- ✅ "Under Review" email after setup completion
- ✅ Email mentions 24-hour review period

### Test Scenario 2: Super Admin Approval

**Steps:**
1. Super Admin approves restaurant
2. Check restaurant admin email

**Expected:**
- ✅ Approval email received
- ✅ Restaurant goes live

### Test Scenario 3: Super Admin Rejection

**Steps:**
1. Super Admin rejects restaurant with reason
2. Check restaurant admin email

**Expected:**
- ✅ Rejection email received
- ✅ Reason clearly stated
- ✅ Instructions for resubmission

---

## Benefits of New Flow

✅ **Clearer Communication** - Restaurant admins know exactly what to expect  
✅ **No Confusion** - No OTP verification needed  
✅ **Better UX** - Single email after setup completion  
✅ **Realistic Expectations** - 24-hour review period clearly stated  
✅ **Professional** - Matches real-world business approval processes  

---

## Summary

| Event | Old Email | New Email |
|-------|-----------|-----------|
| Admin Registration | Welcome Email | None |
| Setup Complete | OTP Email | Under Review Email |
| Super Admin Approves | None | Approval Email |
| Super Admin Rejects | None | Rejection Email |

The new flow provides a clearer, more professional experience for restaurant admins! 🎉
