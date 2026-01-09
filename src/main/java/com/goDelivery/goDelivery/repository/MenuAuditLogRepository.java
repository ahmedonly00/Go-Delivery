package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.MenuAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MenuAuditLogRepository extends JpaRepository<MenuAuditLog, Long> {

    List<MenuAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    List<MenuAuditLog> findByBranchIdOrderByCreatedAtDesc(Long branchId);

    List<MenuAuditLog> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<MenuAuditLog> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    @Query("SELECT a FROM MenuAuditLog a WHERE a.branchId = :branchId AND a.createdAt >= :startDate ORDER BY a.createdAt DESC")
    List<MenuAuditLog> findRecentByBranchId(@Param("branchId") Long branchId, @Param("startDate") LocalDate startDate);

    @Query("SELECT a FROM MenuAuditLog a WHERE a.action = :action AND a.createdAt >= :startDate ORDER BY a.createdAt DESC")
    List<MenuAuditLog> findByActionAndDate(@Param("action") String action, @Param("startDate") LocalDate startDate);

    @Query("SELECT COUNT(a) FROM MenuAuditLog a WHERE a.branchId = :branchId AND a.createdAt = :date")
    long countByBranchIdAndDate(@Param("branchId") Long branchId, @Param("date") LocalDate date);
}
