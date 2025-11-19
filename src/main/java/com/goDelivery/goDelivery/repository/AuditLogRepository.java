package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Custom query methods can be added here if needed
}
