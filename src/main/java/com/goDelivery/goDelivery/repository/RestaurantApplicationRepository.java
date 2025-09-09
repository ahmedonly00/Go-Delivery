package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.Enum.ApplicationStatus;
import com.goDelivery.goDelivery.model.RestaurantApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantApplicationRepository extends JpaRepository<RestaurantApplication, Long> {
    
    boolean existsByEmail(String email);
    
    Optional<RestaurantApplication> findByEmail(String email);
    
    Page<RestaurantApplication> findByApplicationStatus(ApplicationStatus status, Pageable pageable);
}
