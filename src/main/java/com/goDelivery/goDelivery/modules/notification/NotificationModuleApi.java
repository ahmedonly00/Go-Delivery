package com.goDelivery.goDelivery.modules.notification;

/**
 * Public API boundary for the Notification module.
 *
 * Exposed services:
 *   - NotificationService → multi-channel notifications (push, in-app)
 *   - EmailService        → email sending with templates
 *   - SmsService          → SMS via Twilio
 *
 * Exposed DTOs:
 *   - NotificationRequest / NotificationResponse
 *
 * Dependencies on other modules:
 *   - shared/config → EmailConfig, AsyncConfig (for async sending)
 *
 * Usage pattern — fire and forget via Spring events:
 *   Other modules publish events (e.g. OrderPlacedEvent),
 *   this module listens and sends the appropriate notification.
 *   Direct calls to NotificationService are also acceptable.
 */
public final class NotificationModuleApi {
    private NotificationModuleApi() {}
}
