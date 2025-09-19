package com.goDelivery.goDelivery.service;

// import jakarta.mail.MessagingException;
// import jakarta.mail.internet.MimeMessage;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.mail.MailException;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.mail.javamail.MimeMessageHelper;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;

// @Service
// public class EmailService {
//     private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
//     private final JavaMailSender mailSender;
//     private final boolean emailSendingEnabled = true; // Set to false to disable email sending
//     private final String fromEmail = "ahmedndayizeye45@gmail.com";
//     private final String fromName = "GoDelivery Support";

//     public EmailService(JavaMailSender mailSender) {
//         this.mailSender = mailSender;
//         logger.info("EmailService initialized. Email sending is {}", emailSendingEnabled ? "ENABLED" : "DISABLED");
//     }

//     @Async
//     public void sendWelcomeEmail(String to, String businessName, String username, String password) {
//         if (to == null || to.isBlank()) {
//             logger.error("Cannot send welcome email: recipient email is null or empty");
//             return;
//         }
        
//         logger.info("Preparing to send welcome email to: {}, Business: {}, Username: {}", 
//                    to, businessName, username);
        
//         if (!emailSendingEnabled) {
//             logger.warn("Email sending is currently disabled. Set emailSendingEnabled to true to enable emails.");
//             return;
//         }
        
//         try {
//             MimeMessage mimeMessage = mailSender.createMimeMessage();
//             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
//             String text = String.format(
//                 "<html><body>" +
//                 "<h2>Welcome to GoDelivery - Your Restaurant Account</h2>" +
//                 "<p>Hello %s,</p>" +
//                 "<p>Your restaurant <strong>%s</strong> has been approved on GoDelivery.</p>" +
//                 "<p>You can now log in using the following credentials:</p>" +
//                 "<ul>" +
//                 "<li><strong>Username:</strong> %s</li>" +
//                 "<li><strong>Password:</strong> %s</li>" +
//                 "</ul>" +
//                 "<p>Please change your password after first login for security reasons.</p>" +
//                 "<p>Best regards,<br>GoDelivery Team</p>" +
//                 "</body></html>",
//                 businessName, businessName, username, password
//             );
            
//             helper.setFrom(fromEmail, fromName);
//             helper.setTo(to);
//             helper.setSubject("Welcome to GoDelivery - Your Restaurant Account");
//             helper.setText(text, true); // true = isHtml
            
//             logger.debug("Sending welcome email to: {}", to);
//             mailSender.send(mimeMessage);
//             logger.info("Welcome email sent successfully to: {}", to);
            
//         } catch (MessagingException e) {
//             logger.error("Failed to create welcome email for: {}", to, e);
//         } catch (MailException e) {
//             logger.error("Failed to send welcome email to: {}", to, e);
//         } catch (Exception e) {
//             logger.error("Unexpected error while sending welcome email to: {}", to, e);
//         }
//     }
    
//     @Async
//     public void sendApplicationStatusEmail(String to, String subject, String businessName, String status, String message) {
//         if (to == null || to.isBlank()) {
//             logger.error("Cannot send application status email: recipient email is null or empty");
//             return;
//         }
        
//         logger.info("Preparing to send application status email to: {}, Subject: {}, Status: {}", 
//                    to, subject, status);
        
//         if (!emailSendingEnabled) {
//             logger.warn("Email sending is currently disabled. Set emailSendingEnabled to true to enable emails.");
//             return;
//         }
        
//         try {
//             MimeMessage mimeMessage = mailSender.createMimeMessage();
//             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
//             String statusColor = "approved".equalsIgnoreCase(status) ? "#28a745" : 
//                                ("rejected".equalsIgnoreCase(status) ? "#dc3545" : "#6c757d");
            
//             String text = String.format(
//                 "<html><body>" +
//                 "<h2>Application Status Update: %s</h2>" +
//                 "<p>Hello %s,</p>" +
//                 "<p>The status of your restaurant application has been updated.</p>" +
//                 "<div style=\"background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 15px 0;\">" +
//                 "<p><strong>Business Name:</strong> %s</p>" +
//                 "<p><strong>Status:</strong> <span style=\"color: %s; font-weight: bold;\">%s</span></p>" +
//                 "<p><strong>Message:</strong> %s</p>" +
//                 "</div>" +
//                 "<p>If you have any questions, please contact our support team.</p>" +
//                 "<p>Best regards,<br>GoDelivery Team</p>" +
//                 "</body></html>",
//                 status.toUpperCase(), businessName, businessName, statusColor, status.toUpperCase(), message
//             );
            
//             helper.setFrom(fromEmail, fromName);
//             helper.setTo(to);
//             helper.setSubject(subject);
//             helper.setText(text, true); // true = isHtml
            
//             logger.debug("Sending application status email to: {}", to);
//             mailSender.send(mimeMessage);
//             logger.info("Application status email sent successfully to: {}", to);
            
//         } catch (MessagingException e) {
//             logger.error("Failed to create application status email for: {}", to, e);
//         } catch (MailException e) {
//             logger.error("Failed to send application status email to: {}", to, e);
//         } catch (Exception e) {
//             logger.error("Unexpected error while sending application status email to: {}", to, e);
//         }
//     }
// }


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendApplicationStatusEmail(String to, String subject, String businessName, String status, String message) {
        try {
            log.info("Preparing to send application status email to {}", to);
            System.out.println("Mail username: " + fromEmail);
            
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(
                "Dear " + businessName + ",\n\n" +
                message + "\n\n" +
                "Status: " + status + "\n\n" +
                "Regards,\nRestaurant Management Team"
            );

            mailSender.send(mailMessage);
            log.info("Application status email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send application status email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send application status email: " + e.getMessage(), e);
        }
    }

    public void sendWelcomeEmail(String to, String businessName, String username, String password) {
        try {
            log.info("Preparing to send welcome email to {}", to);

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject("Welcome to " + businessName + " Platform");
            mailMessage.setText(
                "Hello,\n\n" +
                "Welcome to " + businessName + "! Your account has been created successfully.\n\n" +
                "Username: " + username + "\n" +
                "Password: " + password + "\n\n" +
                "Please log in and change your password as soon as possible.\n\n" +
                "Regards,\nRestaurant Management Team"
            );

            mailSender.send(mailMessage);
            log.info("Welcome email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email: " + e.getMessage(), e);
        }
    }

    // Test method for debugging email sending independently
    public void sendTestEmail(String to) {
        sendApplicationStatusEmail(
            to,
            "Test Email",
            "Test Business",
            "approved",
            "This is a test email to verify SMTP configuration."
        );
    }
}

