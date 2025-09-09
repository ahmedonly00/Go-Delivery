package com.goDelivery.goDelivery.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendWelcomeEmail(String to, String businessName, String username, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set email properties
            helper.setTo(to);
            helper.setSubject("Welcome to GoDelivery - Your Restaurant Account");
            
            // Create the Thymeleaf context and add variables
            Context context = new Context();
            context.setVariable("businessName", businessName);
            context.setVariable("username", username);
            context.setVariable("password", password);
            
            // Process the HTML template
            String htmlContent = templateEngine.process("email/welcome-email", context);
            
            // Set the HTML content
            helper.setText(htmlContent, true);
            
            // Send the email
            mailSender.send(message);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }
}
