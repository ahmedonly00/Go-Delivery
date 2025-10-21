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

import jakarta.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.base-url:http://localhost:8085}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        log.info("=".repeat(60));
        log.info("📧 EMAIL CONFIGURATION LOADED");
        log.info("=".repeat(60));
        log.info("From Email: {}", fromEmail != null && !fromEmail.isEmpty() ? fromEmail : "❌ NOT CONFIGURED");
        log.info("SMTP Host: smtp.gmail.com");
        log.info("SMTP Port: 587");
        log.info("Frontend URL: {}", frontendUrl);
        log.info("Base URL: {}", baseUrl);
        log.info("=".repeat(60));
        
        if (fromEmail == null || fromEmail.isEmpty() || fromEmail.equals("your-email@gmail.com")) {
            log.error("⚠️  WARNING: Email is not properly configured!");
            log.error("⚠️  Please update MAIL_USERNAME in your .env file");
        }
    }
 
    @Async
    public void sendSetupCompletionEmail(String toEmail, String name, String restaurantName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Restaurant Setup Complete - " + restaurantName);
            
            // Create email content using Thymeleaf
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("restaurantName", restaurantName);
            
            String htmlContent = templateEngine.process("setup-complete", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Restaurant setup completion email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send setup completion email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send setup completion email", e);
        }
    }

    @Async
    public void sendOtpEmail(String to, String name, String otp) {
        try {
            log.info("Starting OTP email preparation for: {}", to);
            log.debug("Using fromEmail: {}, SMTP host configured", fromEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("GoDelivery <" + fromEmail + ">");
            helper.setTo(to);
            helper.setSubject("Verify Your Email - GoDelivery");

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("otp", otp);
            context.setVariable("frontendUrl", frontendUrl);

            log.debug("Processing email template: otp-verification");
            String htmlContent = templateEngine.process("otp-verification", context);
            helper.setText(htmlContent, true);
            
            log.info("Sending OTP email to: {}", to);
            mailSender.send(message);
            log.info("✅ OTP email successfully sent to: {}", to);
            
        } catch (MessagingException e) {
            log.error("❌ MessagingException while sending OTP email to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send OTP email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error while sending OTP email to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }
    
    @Async
    public CompletableFuture<Boolean> sendTestEmail(String to) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Test Email from Go Delivery");
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
            
            helper.setFrom("GoDelivery <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject("Verify your email address");
            
            // Redirect to frontend verification page with token
            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
            
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("verificationUrl", verificationUrl);
            
            String htmlContent = templateEngine.process("verification-email", context);
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
            
            helper.setFrom("GoDelivery <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Go Delivery!");
            
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("restaurantName", restaurantName);
            context.setVariable("dashboardUrl", frontendUrl + "/dashboard");
            
            String htmlContent = templateEngine.process("welcome-email", context);
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
            
            helper.setFrom("GoDelivery <" + fromEmail + ">");
            helper.setTo(ownerEmail);
            helper.setSubject("Welcome to Go Delivery - Your Restaurant Account is Ready!");
            
            Context context = new Context();
            context.setVariable("name", ownerName);
            context.setVariable("restaurantName", restaurantName);
            context.setVariable("temporaryPassword", temporaryPassword);
            context.setVariable("loginUrl", frontendUrl + "/login");
            
            String htmlContent = templateEngine.process("restaurant-welcome-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Restaurant welcome email sent to: {}", ownerEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send restaurant welcome email to {}: {}", ownerEmail, e.getMessage());
            throw new EmailSendingException("Failed to send restaurant welcome email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("GoDelivery <" + fromEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");
            
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetUrl", resetUrl);
            
            String htmlContent = templateEngine.process("password-reset-email", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Failed to send password reset email", e);
        }
    }
}
