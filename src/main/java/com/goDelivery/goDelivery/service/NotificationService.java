package com.goDelivery.goDelivery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class NotificationService {

    public void sendEmail(String toEmail, String subject, String templateName, Map<String, Object> templateData) {
        // In a real implementation, this would send an email using a service like SendGrid, AWS SES, etc.
        log.info("Sending email to: {}", toEmail);
        log.info("Subject: {}", subject);
        log.info("Template: {}", templateName);
        log.info("Template data: {}", templateData);
        
        // For development, we'll just log the email content
        String message = String.format("""
            To: %s
            Subject: %s
            
            %s
            """, toEmail, subject, templateData.get("message"));
            
        log.info("Email content:\n{}", message);
    }

    public void sendSms(String phoneNumber, String message) {
        // In a real implementation, this would send an SMS using a service like Twilio, etc.
        log.info("Sending SMS to: {}", phoneNumber);
        log.info("Message: {}", message);
    }

    public void sendPushNotification(Long userId, String title, String message, Map<String, String> data) {
        // In a real implementation, this would send a push notification using Firebase Cloud Messaging, etc.
        log.info("Sending push notification to user: {}", userId);
        log.info("Title: {}", title);
        log.info("Message: {}", message);
        if (data != null && !data.isEmpty()) {
            log.info("Additional data: {}", data);
        }
    }

    public void notifyBikerNewOrder(Long bikerId, String bikerEmail, String bikerPhone, 
                                     String orderNumber, String restaurantName, 
                                     String pickupAddress, String deliveryAddress) {
        // In a real implementation, this would send notifications via multiple channels
        
        String title = "New Order Available!";
        String message = String.format("""
            New order #%s is ready for pickup!
            Restaurant: %s
            Pickup: %s
            Delivery: %s
            
            Please confirm acceptance in the app.
            """, orderNumber, restaurantName, pickupAddress, deliveryAddress);
        
        // Send push notification
        Map<String, String> pushData = new HashMap<>();
        pushData.put("orderNumber", orderNumber);
        pushData.put("restaurantName", restaurantName);
        pushData.put("pickupAddress", pickupAddress);
        pushData.put("deliveryAddress", deliveryAddress);
        pushData.put("type", "NEW_ORDER");
        
        sendPushNotification(bikerId, title, message, pushData);
        
        // Send SMS notification
        String smsMessage = String.format("New order #%s ready at %s. Check app for details.", 
                orderNumber, restaurantName);
        sendSms(bikerPhone, smsMessage);
        
        // Send email notification
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("orderNumber", orderNumber);
        emailData.put("restaurantName", restaurantName);
        emailData.put("pickupAddress", pickupAddress);
        emailData.put("deliveryAddress", deliveryAddress);
        emailData.put("message", message);
        
        sendEmail(bikerEmail, "New Delivery Order #" + orderNumber, "biker-new-order", emailData);
        
        log.info("Sent new order notification to biker {} for order {}", bikerId, orderNumber);
    }

    public void notifyAvailableBikersForOrder(String orderNumber, String restaurantName, 
                                               String pickupAddress, String deliveryAddress) {
        // In a real implementation, this would query available bikers and send notifications
        // For now, this is a placeholder for broadcasting to available bikers
        
        log.info("Broadcasting new order {} to all available bikers", orderNumber);
        log.info("Restaurant: {}, Pickup: {}, Delivery: {}", 
                restaurantName, pickupAddress, deliveryAddress);
        
        // In a real implementation:
        // 1. Query BikersRepository for available online bikers
        // 2. Calculate distance to restaurant for each biker
        // 3. Send notifications to bikers within reasonable distance
        // 4. Implement first-come-first-served or intelligent assignment logic
    }
    
    public void notifyDeliveryAccepted(com.goDelivery.goDelivery.model.Order order, 
                                        com.goDelivery.goDelivery.model.Bikers biker, 
                                        Integer estimatedDeliveryMinutes) {
        // Notify customer that biker accepted delivery
        if (order.getCustomer() != null) {
            String customerEmail = order.getCustomer().getEmail();
            String customerMessage = String.format("""
                Good news! Your order #%s has been accepted by %s.
                
                Delivery Person: %s
                Rating: %.1f
                Phone: %s
                Estimated Delivery: %d minutes
                
                You can track your order in real-time through the app.
                """, 
                order.getOrderNumber(), 
                biker.getFullName(),
                biker.getFullName(),
                biker.getRating() != null ? biker.getRating() : 4.5,
                biker.getPhoneNumber(),
                estimatedDeliveryMinutes != null ? estimatedDeliveryMinutes : 30
            );
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("bikerName", biker.getFullName());
            emailData.put("bikerPhone", biker.getPhoneNumber());
            emailData.put("estimatedMinutes", estimatedDeliveryMinutes);
            emailData.put("message", customerMessage);
            
            sendEmail(customerEmail, "Your Delivery is On The Way!", "delivery-accepted", emailData);
            
            log.info("Notified customer {} that biker {} accepted order {}", 
                    order.getCustomer().getEmail(), biker.getBikerId(), order.getOrderNumber());
        }
        
        // Notify restaurant that biker accepted
        if (order.getRestaurant() != null && order.getRestaurant().getEmail() != null) {
            String restaurantMessage = String.format("""
                Biker %s has accepted delivery for order #%s.
                
                Biker Details:
                Name: %s
                Phone: %s
                Vehicle: %s - %s
                
                Order should be ready for pickup soon.
                """,
                biker.getFullName(),
                order.getOrderNumber(),
                biker.getFullName(),
                biker.getPhoneNumber(),
                biker.getVehicleType(),
                biker.getVehiclePlate()
            );
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("bikerName", biker.getFullName());
            emailData.put("message", restaurantMessage);
            
            sendEmail(order.getRestaurant().getEmail(), 
                    "Biker Assigned - Order #" + order.getOrderNumber(), 
                    "biker-assigned", emailData);
        }
        
        log.info("Sent delivery acceptance notifications for order {}", order.getOrderNumber());
    }
    
    public void notifyDeliveryRejected(com.goDelivery.goDelivery.model.Order order, 
                                        com.goDelivery.goDelivery.model.Bikers biker, 
                                        String reason) {
        // Notify restaurant that biker rejected delivery
        if (order.getRestaurant() != null && order.getRestaurant().getEmail() != null) {
            String message = String.format("""
                Biker %s has declined delivery for order #%s.
                Reason: %s
                
                The order has been broadcast to other available bikers.
                """,
                biker.getFullName(),
                order.getOrderNumber(),
                reason
            );
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("bikerName", biker.getFullName());
            emailData.put("reason", reason);
            emailData.put("message", message);
            
            sendEmail(order.getRestaurant().getEmail(), 
                    "Delivery Declined - Order #" + order.getOrderNumber(), 
                    "delivery-rejected", emailData);
            
            log.info("Notified restaurant that biker {} rejected order {} - Reason: {}", 
                    biker.getBikerId(), order.getOrderNumber(), reason);
        }
    }
    
    public void notifyPickupConfirmed(com.goDelivery.goDelivery.model.Order order, 
                                       com.goDelivery.goDelivery.model.Bikers biker) {
        // Notify customer that order is picked up and on the way
        if (order.getCustomer() != null) {
            String customerEmail = order.getCustomer().getEmail();
            String customerMessage = String.format("""
                Great news! Your order #%s has been picked up and is on its way!
                
                Delivery Person: %s
                Phone: %s
                Vehicle: %s - %s
                Estimated Arrival: 15-20 minutes
                
                You can track your order in real-time through the app.
                Your food will arrive hot and fresh!
                """, 
                order.getOrderNumber(),
                biker.getFullName(),
                biker.getPhoneNumber(),
                biker.getVehicleType(),
                biker.getVehiclePlate()
            );
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("bikerName", biker.getFullName());
            emailData.put("bikerPhone", biker.getPhoneNumber());
            emailData.put("vehicleInfo", biker.getVehicleType() + " - " + biker.getVehiclePlate());
            emailData.put("message", customerMessage);
            
            sendEmail(customerEmail, "Your Order is On The Way!", "order-picked-up", emailData);
            
            // Send SMS for immediate notification
            String smsMessage = String.format("Your order #%s has been picked up by %s and is on its way! ETA: 15-20 min. Track: [app link]", 
                    order.getOrderNumber(), biker.getFullName());
            sendSms(order.getCustomer().getPhoneNumber(), smsMessage);
            
            // Send push notification
            Map<String, String> pushData = new HashMap<>();
            pushData.put("orderNumber", order.getOrderNumber());
            pushData.put("orderId", order.getOrderId().toString());
            pushData.put("bikerName", biker.getFullName());
            pushData.put("type", "ORDER_PICKED_UP");
            
            sendPushNotification(
                order.getCustomer().getCustomerId(),
                "Order Picked Up!",
                "Your order is on its way. Estimated arrival: 15-20 minutes.",
                pushData
            );
            
            log.info("Notified customer {} that order {} was picked up by biker {}", 
                    order.getCustomer().getEmail(), order.getOrderNumber(), biker.getBikerId());
        }
        
        // Notify restaurant that order was successfully picked up
        if (order.getRestaurant() != null && order.getRestaurant().getEmail() != null) {
            String restaurantMessage = String.format("""
                Order #%s has been picked up by %s.
                
                Biker: %s
                Pickup Time: %s
                Customer: %s
                Delivery Address: %s
                
                The order is now en route to the customer.
                """,
                order.getOrderNumber(),
                biker.getFullName(),
                biker.getFullName(),
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")),
                order.getCustomer() != null ? order.getCustomer().getFullName() : "Customer",
                order.getDeliveryAddress()
            );
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("bikerName", biker.getFullName());
            emailData.put("message", restaurantMessage);
            
            sendEmail(order.getRestaurant().getEmail(), 
                    "Order Picked Up - #" + order.getOrderNumber(), 
                    "order-dispatched", emailData);
        }
        
        log.info("Sent pickup confirmation notifications for order {}", order.getOrderNumber());
    }
    
    public void notifyCustomerLocationUpdate(com.goDelivery.goDelivery.model.Order order, 
                                              com.goDelivery.goDelivery.model.Bikers biker,
                                              com.goDelivery.goDelivery.dtos.delivery.LocationUpdateRequest locationUpdate) {
        // In production, this would send real-time location updates to customer via WebSocket
        // For now, log the location update
        
        log.info("Biker {} location update for order {}: lat={}, lon={}", 
                biker.getBikerId(), order.getOrderNumber(), 
                locationUpdate.getLatitude(), locationUpdate.getLongitude());
        
        // Calculate ETA based on distance and speed
        // Send push notification to customer with updated ETA if significant change
        
        log.info("Customer tracking update sent for order {}", order.getOrderNumber());
    }
    
    public void notifyDeliveryCompleted(com.goDelivery.goDelivery.model.Order order, 
                                         com.goDelivery.goDelivery.model.Bikers biker,
                                         com.goDelivery.goDelivery.dtos.delivery.DeliveryConfirmationRequest confirmation) {
        // Notify customer that delivery is complete
        if (order.getCustomer() != null) {
            String customerEmail = order.getCustomer().getEmail();
            String customerMessage = String.format("""
                Your order #%s has been successfully delivered!
                
                Delivered by: %s
                Delivery Time: %s
                %s
                
                We hope you enjoy your meal!
                Please rate your delivery experience in the app.
                """, 
                order.getOrderNumber(),
                biker.getFullName(),
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")),
                confirmation.getContactlessDelivery() != null && confirmation.getContactlessDelivery() 
                    ? "Contactless delivery completed" 
                    : "Received by: " + (confirmation.getRecipientName() != null ? confirmation.getRecipientName() : "Customer")
            );
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("bikerName", biker.getFullName());
            emailData.put("deliveryTime", java.time.LocalDateTime.now().toString());
            emailData.put("message", customerMessage);
            
            sendEmail(customerEmail, "Order Delivered - #" + order.getOrderNumber(), "order-delivered", emailData);
            
            // Send push notification
            Map<String, String> pushData = new HashMap<>();
            pushData.put("orderNumber", order.getOrderNumber());
            pushData.put("orderId", order.getOrderId().toString());
            pushData.put("type", "ORDER_DELIVERED");
            
            sendPushNotification(
                order.getCustomer().getCustomerId(),
                "Order Delivered!",
                "Your order has been successfully delivered. Enjoy your meal!",
                pushData
            );
            
            log.info("Notified customer {} that order {} was delivered", 
                    order.getCustomer().getEmail(), order.getOrderNumber());
        }
        
        // Notify restaurant of successful delivery
        if (order.getRestaurant() != null && order.getRestaurant().getEmail() != null) {
            String restaurantMessage = String.format("""
                Order #%s has been successfully delivered.
                
                Biker: %s
                Customer: %s
                Delivery Address: %s
                Delivery Time: %s
                Order Amount: $%.2f
                
                Order completed successfully.
                """,
                order.getOrderNumber(),
                biker.getFullName(),
                order.getCustomer() != null ? order.getCustomer().getFullName() : "Customer",
                order.getDeliveryAddress(),
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")),
                order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0
            );
            
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("orderNumber", order.getOrderNumber());
            emailData.put("bikerName", biker.getFullName());
            emailData.put("message", restaurantMessage);
            
            sendEmail(order.getRestaurant().getEmail(), 
                    "Order Completed - #" + order.getOrderNumber(), 
                    "order-completed", emailData);
        }
        
        log.info("Sent delivery completion notifications for order {}", order.getOrderNumber());
    }
}
