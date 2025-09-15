package com.goDelivery.goDelivery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final boolean emailSendingEnabled = true; // Set to false to disable email sending

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        logger.info("EmailService initialized. Email sending is {}", emailSendingEnabled ? "ENABLED" : "DISABLED");
    }

    @Async
    public void sendWelcomeEmail(String to, String businessName, String username, String password) {
        logger.info("[EMAIL DISABLED] Would send welcome email to: {}, Business: {}, Username: {}", 
                   to, businessName, username);
        
        if (emailSendingEnabled) {
            logger.warn("Email sending is currently disabled. Set emailSendingEnabled to true to enable emails.");
        }
    }
    
    @Async
    public void sendApplicationStatusEmail(String to, String subject, String businessName, String status, String message) {
        logger.info("[EMAIL DISABLED] Would send application status email to: {}, Subject: {}, Status: {}", 
                   to, subject, status);
        logger.info("Business: {}, Message: {}", businessName, message);
        
        if (emailSendingEnabled) {
            logger.warn("Email sending is currently disabled. Set emailSendingEnabled to true to enable emails.");
        }
    }
}
