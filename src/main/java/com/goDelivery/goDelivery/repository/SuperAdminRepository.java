package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {
    // Additional custom query methods can be added here if needed
}
