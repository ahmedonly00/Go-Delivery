package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Bikers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BikersRepository extends JpaRepository<Bikers, Long> {

    Optional<Bikers> findByBikerId(Long bikerId);
    boolean existsById(Bikers bikerId);
    
    @Query("SELECT b FROM Bikers b WHERE b.isOnline = true AND b.isAvailable = true AND b.isActive = true")
    List<Bikers> findAvailableBikers();
    
    Optional<Bikers> findByEmail(String email);
    
    Optional<Bikers> findByPhoneNumber(String phoneNumber);
    
    Optional<Bikers> findByLicenseNumber(String licenseNumber);

    Optional<Bikers> findByIsActiveTrue();

}
