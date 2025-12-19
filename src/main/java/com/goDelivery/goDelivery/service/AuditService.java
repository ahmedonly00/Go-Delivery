package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.model.AuditLog;
import com.goDelivery.goDelivery.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

   
    @Transactional
    public void logPaymentWebhook(String transactionId, String status, String details) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEventType("PAYMENT_WEBHOOK");
            auditLog.setEntityType("Payment");
            auditLog.setEntityId(transactionId);
            auditLog.setStatus(status);
            auditLog.setDetails(details);
            auditLog.setCreatedAt(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
            log.debug("Logged payment webhook for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to log payment webhook for transaction {}: {}", transactionId, e.getMessage(), e);
            // Don't throw exception as we don't want to fail the main operation
        }
    }

    
    @Transactional
    public void logPaymentAction(Long paymentId, String action, String status, String details) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEventType("PAYMENT_" + action.toUpperCase());
            auditLog.setEntityType("Payment");
            auditLog.setEntityId(String.valueOf(paymentId));
            auditLog.setStatus(status);
            auditLog.setDetails(details);
            auditLog.setCreatedAt(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
            log.debug("Logged payment action for payment {}: {}/{}", paymentId, action, status);
        } catch (Exception e) {
            log.error("Failed to log payment action for payment {}: {}", paymentId, e.getMessage(), e);
            // Don't throw exception as we don't want to fail the main operation
        }
    }
}
