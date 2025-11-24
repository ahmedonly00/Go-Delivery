package com.goDelivery.goDelivery.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Production implementation of SmsService using Twilio for sending SMS messages.
 * This service is activated when sms.test-mode is set to false in application properties.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.notifications.sms.test-mode", havingValue = "false", matchIfMissing = false)
public class TwilioSmsService implements SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio SMS Service initialized with account SID: {}", accountSid);
    }

    @Override
    public boolean sendSms(String toPhoneNumber, String message) {
        try {
            // Ensure the phone number is in E.164 format (e.g., +1234567890)
            String formattedNumber = formatPhoneNumber(toPhoneNumber);
            
            Message.creator(
                new PhoneNumber(formattedNumber),
                new PhoneNumber(fromPhoneNumber),
                message
            ).create();
            
            log.info("SMS sent to {}: {}", formattedNumber, message);
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Formats a phone number to E.164 format required by Twilio
     */
    private String formatPhoneNumber(String phoneNumber) {
        // Remove all non-digit characters
        String digits = phoneNumber.replaceAll("\\D+", "");
        
        // If the number doesn't start with a '+', add the default country code
        if (!phoneNumber.startsWith("+")) {
            // Default country code (Kenya in this case)
            String defaultCountryCode = "254";
            
            // Remove leading zero if present and add country code
            if (digits.startsWith("0")) {
                digits = defaultCountryCode + digits.substring(1);
            } else if (!digits.startsWith(defaultCountryCode)) {
                digits = defaultCountryCode + digits;
            }
            
            return "+" + digits;
        }
        
        return "+" + digits;
    }
}
