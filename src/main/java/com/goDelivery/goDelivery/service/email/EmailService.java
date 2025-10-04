package com.goDelivery.goDelivery.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.goDelivery.goDelivery.exception.EmailSendingException;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public CompletableFuture<Boolean> sendTestEmail(String to) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Test Email");
            helper.setText("This is a test email from Go Delivery");
            
            mailSender.send(message);
            log.info("Test email sent to: {}", to);
            return CompletableFuture.completedFuture(true);
            
        } catch (MessagingException e) {
            log.error("Failed to send test email to {}: {}", to, e.getMessage());
            return CompletableFuture.failedFuture(new EmailSendingException("Failed to send test email", e));
        }
    }

    @Async
    public void sendVerificationEmail(String toEmail, String name, String verificationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your email address");
            
            String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + verificationToken;
            
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("verificationUrl", verificationUrl);
            
            String htmlContent = templateEngine.process("emails/verification-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Failed to send verification email", e);
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String name, String restaurantName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Our Platform - Start Managing Your Restaurant");
            
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("restaurantName", restaurantName);
            context.setVariable("dashboardUrl", baseUrl + "/dashboard");
            
            String htmlContent = templateEngine.process("emails/welcome-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Failed to send welcome email", e);
        }
    }

    @Async
    public void sendRestaurantWelcomeEmail(String restaurantName, String ownerEmail, String ownerName, String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(ownerEmail);
            helper.setSubject("Welcome to Go Delivery - Your Restaurant Account is Ready!");
            
            Context context = new Context();
            context.setVariable("name", ownerName);
            context.setVariable("restaurantName", restaurantName);
            context.setVariable("temporaryPassword", temporaryPassword);
            context.setVariable("loginUrl", baseUrl + "/login");
            
            String htmlContent = templateEngine.process("emails/restaurant-welcome-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Restaurant welcome email sent to: {}", ownerEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send restaurant welcome email to {}: {}", ownerEmail, e.getMessage());
            throw new EmailSendingException("Failed to send restaurant welcome email", e);
        }
    }

    @Async
    public void sendApplicationRejectionEmail(String restaurantName, String ownerEmail, String rejectionReason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(ownerEmail);
            helper.setSubject("Your Restaurant Application Status");
            
            Context context = new Context();
            context.setVariable("restaurantName", restaurantName);
            context.setVariable("rejectionReason", rejectionReason);
            context.setVariable("contactUrl", baseUrl + "/contact");
            
            String htmlContent = templateEngine.process("emails/application-rejection-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Application rejection email sent to: {}", ownerEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send application rejection email to {}: {}", ownerEmail, e.getMessage());
            throw new EmailSendingException("Failed to send application rejection email", e);
        }
    }

    @Async
    public void sendSetupCompletionEmail(String toEmail, String name, String restaurantName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Restaurant is Now Live!");
            
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("restaurantName", restaurantName);
            context.setVariable("dashboardUrl", baseUrl + "/dashboard");
            
            String htmlContent = templateEngine.process("emails/setup-complete-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Setup completion email sent to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send setup completion email to {}: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Failed to send setup completion email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");
            
            String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
            
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetUrl", resetUrl);
            
            String htmlContent = templateEngine.process("emails/password-reset-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Failed to send password reset email", e);
        }
    }

    public boolean sendEmailWithRetry(String to, String subject, String content) {
        final int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content, true);
                
                mailSender.send(message);
                log.info("Email sent to: {}", to);
                return true;
                
            } catch (MessagingException e) {
                retryCount++;
                log.warn("Failed to send email to {} (attempt {}/{}): {}", to, retryCount, maxRetries, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    log.error("Max retries reached for sending email to {}: {}", to, e.getMessage());
                    return false;
                }
                
                // Exponential backoff
                try {
                    Thread.sleep(1000L * (long) Math.pow(2, retryCount - 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        return false;
    }
}
