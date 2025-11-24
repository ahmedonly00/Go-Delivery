package com.goDelivery.goDelivery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Test implementation of SmsService that logs messages instead of sending real SMS.
 * Used when sms.test-mode is set to true in application properties.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.notifications.sms.test-mode", havingValue = "true", matchIfMissing = true)
public class TestSmsService implements SmsService {

    @Override
    public boolean sendSms(String toPhoneNumber, String message) {
        log.info("[TEST SMS] To: {}, Message: {}", toPhoneNumber, message);
        return true; // Always return true in test mode
    }
}
