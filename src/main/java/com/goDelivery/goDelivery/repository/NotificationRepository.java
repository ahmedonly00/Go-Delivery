package com.goDelivery.goDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
}
