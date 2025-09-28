package com.goDelivery.goDelivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements EmailServiceInterface {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds initial delay

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:https://admin.mozfood.com}")
    private String baseUrl;

    @Override
    @Async
    public void sendRestaurantWelcomeEmail(String restaurantName, String ownerEmail, String ownerName, String temporaryPassword) {
        log.info("Preparing welcome email for restaurant: {} (Owner: {})", restaurantName, ownerEmail);
        
        Context context = new Context();
        context.setVariable("restaurantName", restaurantName);
        context.setVariable("ownerName", ownerName);
        context.setVariable("ownerEmail", ownerEmail);
        context.setVariable("temporaryPassword", temporaryPassword);
        context.setVariable("loginUrl", baseUrl + "/login");
        
        String subject = "Welcome to MozFood - Your Restaurant Account is Ready!";
        String template = "emails/welcome-email";
        
        sendEmailWithRetry(ownerEmail, subject, template, context);
    }

    @Override
    @Async
    public void sendApplicationRejectionEmail(String restaurantName, String ownerEmail, String rejectionReason) {
        log.info("Preparing rejection email for restaurant: {} (Owner: {})", restaurantName, ownerEmail);
        
        Context context = new Context();
        context.setVariable("restaurantName", restaurantName);
        context.setVariable("ownerEmail", ownerEmail);
        context.setVariable("rejectionReason", rejectionReason);
        
        String subject = "Your Restaurant Application Status - MozFood";
        String template = "rejection-email";
        
        sendEmailWithRetry(ownerEmail, subject, template, context);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String email, String resetToken) {
        log.info("Preparing password reset email for: {}", email);
        
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        
        Context context = new Context();
        context.setVariable("resetUrl", resetUrl);
        
        String subject = "Password Reset Request - MozFood";
        String template = "reset-password-email";
        
        sendEmailWithRetry(email, subject, template, context);
    }

    @Override
    public boolean sendEmailWithRetry(String to, String subject, String content) {
        return sendEmailWithRetry(to, subject, content, null);
    }

    private boolean sendEmailWithRetry(String to, String subject, String templateName, Context context) {
        int attempt = 0;
        long delay = RETRY_DELAY_MS;
        Exception lastError = null;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                attempt++;
                
                // If it's a retry, wait before trying again
                if (attempt > 1) {
                    log.info("Retry attempt {} for email to {}", attempt, to);
                    Thread.sleep(delay);
                    delay *= 2; // Exponential backoff
                }

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                
                // Set email properties
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setFrom(fromEmail, "MozFood Support");
                
                // Process template if context is provided
                if (context != null) {
                    String htmlContent = templateEngine.process(templateName, context);
                    helper.setText(htmlContent, true);
                } else {
                    // Fallback to plain text if no template
                    helper.setText("Please enable HTML to view this email.", false);
                }
                
                // Send the email
                mailSender.send(mimeMessage);
                
                log.info("Email sent successfully to {} (attempt {})", to, attempt);
                return true;
                
            } catch (Exception e) {
                lastError = e;
                log.warn("Failed to send email to {} (attempt {}): {}", 
                        to, attempt, e.getMessage());
            }
        }
        
        // If we get here, all attempts failed
        String errorMsg = String.format("Failed to send email to %s after %d attempts", 
                to, MAX_RETRY_ATTEMPTS);
        log.error(errorMsg, lastError);
        
        if (lastError != null) {
            throw new EmailSendingException(errorMsg, lastError);
        } else {
            throw new EmailSendingException(errorMsg);
        }
    }


    // Test method for debugging email sending independently
    @Override
    @Async
    public CompletableFuture<Boolean> sendTestEmail(String to) {
        try {
            log.info("Sending test email to {}", to);
            
            Context context = new Context();
            context.setVariable("restaurantName", "Test Restaurant");
            context.setVariable("ownerName", "Test User");
            context.setVariable("ownerEmail", to);
            context.setVariable("temporaryPassword", "test123");
            
            boolean success = sendEmailWithRetry(to, "Test Email - MozFood", "emails/welcome-email", context);
            return CompletableFuture.completedFuture(success);
            
        } catch (Exception e) {
            log.error("Test email sending failed: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}

