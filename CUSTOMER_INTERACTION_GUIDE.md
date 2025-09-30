# Customer Interaction Guide for Bikers

## Overview
Professional guidelines and best practices for bikers to provide excellent customer service during delivery handover, ensuring positive customer experiences and high ratings.

---

## Core Principles

### 🌟 The 5 Pillars of Excellent Delivery Service

1. **Professionalism** - Always maintain a professional demeanor
2. **Communication** - Keep customers informed throughout delivery
3. **Courtesy** - Treat every customer with respect and kindness
4. **Reliability** - Deliver on time and handle food with care
5. **Problem-Solving** - Handle issues calmly and professionally

---

## Before Arrival

### 1. Review Order Details

**In the app, check:**
- ✅ Customer name
- ✅ Delivery address and apartment/unit number
- ✅ Special instructions
- ✅ Contact preferences (call, text, doorbell)
- ✅ Contactless delivery preference
- ✅ Payment status (prepaid or cash on delivery)

**API Endpoint:**
```http
GET /api/bikers/{bikerId}/activeOrders
```

**Response includes:**
```json
{
  "customerName": "Jane Doe",
  "customerPhone": "+1234567890",
  "deliveryAddress": "456 Oak Avenue, Apartment 3B",
  "specialInstructions": "Ring doorbell twice. No contact delivery.",
  "paymentMethod": "PREPAID",
  "orderItems": [...]
}
```

### 2. Send Arrival Notification

**When 2-3 minutes away:**

**Text Message Template:**
```
Hi Jane! This is John, your delivery driver. I'm 2 minutes away 
with your order from Pizza Palace. I'll ring the doorbell twice 
as requested.
```

**Use this API to send notification:**
```http
POST /api/bikers/notifyArrival
{
  "orderId": 101,
  "bikerId": 1,
  "message": "I'm 2 minutes away with your order"
}
```

### 3. Handle Food with Care

- Keep order upright and level
- Use insulated bag for hot items
- Separate hot and cold items
- Check packaging for any issues
- Ensure drinks are secure

---

## Arrival & Handover

### Standard Delivery (In-Person)

#### Step 1: Park Safely
- Don't block driveways or walkways
- Park legally and safely
- Keep vehicle visible for security

#### Step 2: Approach Professionally
- Walk confidently but not rushed
- Carry order securely
- Have order ready to hand over
- Smile and make eye contact

#### Step 3: Greet Customer
**Opening Lines:**
- ✅ "Hi! Jane? I have your order from Pizza Palace."
- ✅ "Hello! Delivery for Jane?"
- ✅ "Good evening! Your food delivery from Pizza Palace."

**Tone:**
- Friendly but professional
- Clear and audible
- Positive energy

#### Step 4: Verify Identity (for high-value orders)
**Politely ask:**
- "Can I confirm your name is Jane?"
- "Are you Jane Doe?"
- For alcohol: "May I see your ID please?"

#### Step 5: Hand Over Order
**While handing over:**
- "Here's your order. Everything should be hot and fresh."
- "I checked to make sure everything is secure."
- "Your drinks are in the bag on the right."

**Check items if requested:**
- "Would you like me to wait while you check the order?"
- "The receipt is in the bag."

#### Step 6: Professional Close
**Closing Lines:**
- ✅ "Enjoy your meal!"
- ✅ "Have a great evening!"
- ✅ "Thank you and enjoy!"
- ❌ "No worries" (overused)
- ❌ "Peace out" (too casual)

**After handover:**
- Confirm delivery in app immediately
- Take photo if required
- Walk back to vehicle calmly

---

### Contactless Delivery

#### Step 1: Follow Special Instructions
- Read instructions carefully
- "Leave at door" vs "Ring doorbell"
- Note preferred location (porch, side door, etc.)

#### Step 2: Place Order Carefully
- On flat, clean surface
- Away from direct sunlight/rain
- Where it won't be tripped over
- Protected from pets if possible

#### Step 3: Notify Customer
**Ring doorbell OR knock gently**
- Follow customer's preference
- Don't bang or be loud
- Some customers have sleeping babies

**Send text notification:**
```
Hi Jane! Your order has been delivered to your front door. 
Enjoy your meal!
```

#### Step 4: Take Proof Photo
- Show order at location
- Include house number or door
- Good lighting
- Professional angle (not rushed)

#### Step 5: Step Back
- Move 6+ feet away
- Wait briefly (15-30 seconds)
- Ensure customer retrieves order
- If no answer after 30 seconds, leave and confirm in app

---

## Communication Best Practices

### Phone Calls

#### When to Call:
- ✅ Can't find address
- ✅ GPS is unclear
- ✅ Gate code needed
- ✅ Special access required
- ✅ Customer not responding to texts
- ❌ Just to chat
- ❌ To complain about restaurant

#### Phone Script:
```
"Hi, this is John from GoDelivery with your food order. 
I'm having trouble finding [specific issue]. 
Could you help me with [specific question]?"
```

**Be:**
- Brief and specific
- Polite and patient
- Solution-focused

### Text Messages

#### When to Text:
- ✅ 2-3 minutes before arrival
- ✅ Arrived but can't find apartment
- ✅ Customer requested text instead of call
- ✅ Running late (with ETA)

#### Text Templates:

**Arrival:**
```
Hi [Name]! This is [Your Name], your delivery driver. 
I'm arriving now with your order from [Restaurant].
```

**Can't Find Address:**
```
Hi [Name], I'm at [current location] but having trouble 
finding apartment 3B. Could you provide directions?
```

**Running Late:**
```
Hi [Name], I'm running about 5 minutes late due to 
traffic. Your order is on the way! ETA: 12:35 PM.
```

**Contactless Delivery:**
```
Hi [Name]! Your order is at your front door. 
Enjoy your meal!
```

---

## Handling Special Situations

### Customer Not Home

**Steps:**
1. Call customer immediately
2. Wait 3-5 minutes
3. Text customer with photo of location
4. If still no response, follow company protocol:
   - Contact support
   - Leave in safe place (if authorized)
   - Take photo proof

**Message:**
```
Hi [Name], I'm at your address with your order but 
no one is answering. Please let me know where you'd 
like me to leave it, or if you'll be home soon.
```

### Wrong Address

**If customer provides wrong address:**
1. Stay calm and professional
2. Explain situation clearly
3. Offer solutions:
   - Can deliver to correct address if nearby
   - Customer can pick up from current location
   - Contact support for assistance

**Script:**
```
"I'm currently at [address on app], but I understand 
you're actually at [correct address]. Let me check 
if I can deliver there for you."
```

### Missing Items

**If customer reports missing items:**
1. Stay calm and apologetic
2. Check bag thoroughly together
3. Explain what happened
4. Provide solution

**Script:**
```
"I'm so sorry about that. The restaurant packed this 
order, but let me check the bag with you to make sure. 
If something is missing, I'll help you contact support 
right away to get this resolved."
```

**Never:**
- Blame the restaurant
- Argue with customer
- Open sealed containers
- Get defensive

### Food Quality Issues

**If customer complains about food:**
1. Apologize sincerely
2. Acknowledge their concern
3. Explain your role
4. Provide solution

**Script:**
```
"I'm really sorry the food isn't what you expected. 
I delivered it as quickly as possible from the restaurant. 
The best way to resolve this is to contact support 
through the app, and they'll take care of you right away."
```

**Remember:**
- You delivered it, not cooked it
- Be empathetic but don't take blame
- Direct to support for refunds/credits

### Demanding or Rude Customers

**Stay Professional:**
1. Keep calm and composed
2. Listen without interrupting
3. Acknowledge their feelings
4. Provide solutions within your control

**Script:**
```
"I understand you're frustrated, and I want to help. 
Let me see what I can do to make this right."
```

**If situation escalates:**
- Remain calm and polite
- Don't argue or raise voice
- If threatened, leave immediately
- Report to support right away

### Apartment/Complex Deliveries

**Challenges:**
- Multiple buildings
- Confusing layouts
- Gate codes
- Call boxes

**Best Practices:**
1. Call/text for gate code before arrival
2. Ask for specific building number
3. Request landmark (near pool, by mailboxes)
4. Take photo of building map on first visit
5. Save notes for future deliveries

**Script:**
```
"Hi [Name], I'm at the main gate of your complex. 
What's the gate code, and which building are you in?"
```

---

## Professional Appearance

### Dress Code

**Always:**
- ✅ Clean, neat clothing
- ✅ Closed-toe shoes
- ✅ Company uniform/branded gear (if provided)
- ✅ Name badge visible

**Avoid:**
- ❌ Dirty or wrinkled clothes
- ❌ Offensive graphics or text
- ❌ Flip-flops or sandals
- ❌ Strong perfume/cologne

### Personal Hygiene

- Shower daily
- Fresh breath (especially important for close interactions)
- Clean hands and nails
- Neat hair
- Minimal jewelry (safety)

### Vehicle Appearance

**Keep clean:**
- No trash visible
- Clean windows
- Working lights
- Company branding displayed
- Insulated bags clean

---

## Safety Guidelines

### Personal Safety

**Always:**
- Trust your instincts
- Park in well-lit areas
- Keep phone charged
- Be aware of surroundings
- Lock vehicle when leaving
- Don't enter customer homes

**If feeling unsafe:**
- Don't complete delivery
- Leave immediately
- Contact support
- Report incident

### Customer Safety

**Respect:**
- Social distancing
- Contactless preferences
- Privacy (don't look in windows)
- Personal space
- Time (don't linger)

**COVID-19 Protocols:**
- Wear mask if requested
- Maintain distance
- Use contactless delivery when possible
- Hand sanitize between deliveries

---

## Building Customer Rapport

### Small Talk (Keep Brief)

**Good Topics:**
- Weather
- Sports (if customer initiates)
- Compliments on pets (if visible)
- General pleasantries

**Avoid:**
- Politics
- Religion
- Personal problems
- Controversial topics
- Asking personal questions

### Reading the Situation

**Chatty Customer:**
- Be friendly but professional
- Engage briefly
- Politely wrap up: "Enjoy your meal! I have to get the next delivery."

**In a Rush:**
- Be quick and efficient
- Skip small talk
- Hand over and go

**On Phone:**
- Don't interrupt
- Wait patiently
- Nod when acknowledged
- Quick handover when done

---

## Handling Tips

### Cash Tips

**When offered:**
- "Thank you so much, I really appreciate it!"
- Smile genuinely
- Don't count in front of customer
- Show gratitude without being excessive

**If large tip:**
- "Wow, thank you! That's very generous."

### In-App Tips

**After delivery:**
- Customer can add tip in app
- Don't ask for tips
- Don't mention tips
- Quality service leads to better tips

### No Tip

- Never react negatively
- Maintain same professional service
- Many customers tip later in app
- Focus on excellent service

---

## Language and Tone

### Professional Phrases

**Use:**
- ✅ "May I..."
- ✅ "Would you like me to..."
- ✅ "I'd be happy to..."
- ✅ "Thank you for your patience"
- ✅ "I apologize for any inconvenience"

**Avoid:**
- ❌ "Yeah" (use "Yes")
- ❌ "Nah" (use "No")
- ❌ "Whatever"
- ❌ "It's not my fault"
- ❌ "I'm just the driver"

### Positive Language

**Instead of:** "I don't know"  
**Say:** "Let me find out for you"

**Instead of:** "That's not my problem"  
**Say:** "I can help you contact support"

**Instead of:** "The restaurant messed up"  
**Say:** "I apologize for the issue"

**Instead of:** "I can't do that"  
**Say:** "What I can do is..."

---

## Rating and Feedback

### What Customers Rate

**Factors:**
- Timeliness
- Professionalism
- Food condition
- Communication
- Friendliness

### Tips for High Ratings

1. **Arrive on Time** - Track ETA and communicate delays
2. **Handle Food Carefully** - Use bags, keep level
3. **Follow Instructions** - Read and follow special requests
4. **Communicate Well** - Text updates, be responsive
5. **Be Friendly** - Smile, be polite, positive attitude
6. **Look Professional** - Clean appearance, neat clothes
7. **Go Extra Mile** - Offer to wait, help with door, etc.

### After Poor Experience

**If you know service wasn't great:**
- Don't make excuses
- Acknowledge briefly
- "I apologize if anything wasn't perfect today"
- Learn from the experience
- Do better next time

---

## Cultural Sensitivity

### Respect Diversity

**Be aware:**
- Different greeting customs
- Religious considerations
- Language barriers
- Cultural norms

**Always:**
- Treat everyone equally
- Be patient with language differences
- Respect religious items/spaces
- Don't make assumptions

### Language Barriers

**If customer has limited English:**
- Speak slowly and clearly
- Use simple words
- Be patient
- Use gestures if helpful
- Don't speak loudly (not helpful)
- Show respect

---

## Quick Reference Checklist

### Pre-Delivery
- [ ] Review order details
- [ ] Check special instructions
- [ ] Verify address
- [ ] Ensure food is secure
- [ ] Text customer when close

### At Door
- [ ] Park safely
- [ ] Approach professionally
- [ ] Greet customer warmly
- [ ] Verify identity (if needed)
- [ ] Hand over order carefully
- [ ] Thank customer
- [ ] Confirm in app immediately

### Contactless
- [ ] Follow placement instructions
- [ ] Place order safely
- [ ] Ring doorbell/knock
- [ ] Take proof photo
- [ ] Step back
- [ ] Send confirmation text
- [ ] Wait briefly
- [ ] Confirm in app

---

## Training Scenarios

### Scenario 1: Late Delivery
**Situation:** Traffic made you 10 minutes late

**What to do:**
1. Text customer as soon as you know
2. Apologize upon arrival
3. Explain briefly but don't make excuses
4. Thank them for patience

**Script:**
```
"Hi Jane, I'm so sorry for the delay. Traffic was heavier 
than expected. Thank you for your patience. Here's your order, 
and it should still be nice and hot!"
```

### Scenario 2: Spilled Drink
**Situation:** Drink spilled in transit

**What to do:**
1. Assess damage immediately
2. Clean up best you can
3. Call customer before arrival
4. Apologize sincerely
5. Explain what happened
6. Offer to contact support

**Script:**
```
"Hi Jane, I'm calling because unfortunately one of the drinks 
in your order spilled during transport. I'm so sorry about this. 
The food is fine, but I want to make sure you're aware before I 
arrive. I can help you contact support for a refund on the drink."
```

### Scenario 3: Wrong Order
**Situation:** Customer says it's the wrong order

**What to do:**
1. Stay calm
2. Verify order number together
3. Check name and items
4. If indeed wrong, apologize
5. Contact support immediately
6. Follow their instructions

**Script:**
```
"I'm so sorry! Let's check the order number together to make 
sure. [verify] You're absolutely right, this isn't your order. 
I'm going to contact support right now to get this resolved for 
you as quickly as possible."
```

---

## Final Tips for Excellence

### Do's ✅
- Smile genuinely
- Make eye contact
- Use customer's name
- Say "please" and "thank you"
- Follow instructions exactly
- Communicate proactively
- Handle food like it's for your family
- Arrive on time
- Look professional
- Be patient and kind

### Don'ts ❌
- Use phone while at door
- Eat/drink near customer
- Smoke before/during delivery
- Listen to loud music
- Argue with customers
- Make inappropriate comments
- Rush customer
- Look annoyed or tired
- Complain about anything
- Ask for tips

---

## Measuring Success

### Key Performance Indicators

**Track:**
- Customer rating (aim for 4.8+)
- On-time delivery rate (aim for 95%+)
- Customer complaints (aim for <2%)
- Positive feedback mentions
- Repeat customer requests

### Continuous Improvement

**After each shift:**
- Review ratings and feedback
- Identify what went well
- Note areas for improvement
- Adjust behavior accordingly
- Stay positive and motivated

---

## Emergency Contacts

**In Case of:**
- Safety concern
- Accident
- Customer confrontation
- Technical issue
- Food safety concern

**Contact:**
- Support: Available in app
- Emergency: 911 (if serious)
- Company hotline: [To be configured]

---

## Remember

> **Every delivery is an opportunity to make someone's day better.**

Your role isn't just delivering food—it's delivering:
- Convenience
- Comfort
- A positive experience
- Excellent service

**You represent:**
- The restaurant
- The delivery platform
- Yourself as a professional

**Make it count!** 🌟

---

## Related Documentation

- `DELIVERY_CONFIRMATION_GUIDE.md` - Delivery confirmation process
- `PICKUP_CONFIRMATION_GUIDE.md` - Restaurant pickup procedures
- `NAVIGATION_DELIVERY_GUIDE.md` - Navigation and tracking
- `BIKER_FEATURES_SUMMARY.md` - All biker features
- `TESTING_QUICK_REFERENCE.md` - API quick reference

---

**Version:** 1.0  
**Last Updated:** 2025-09-30  
**Purpose:** Professional development and customer service excellence
