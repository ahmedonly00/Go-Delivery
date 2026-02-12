package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Bikers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BikersRepository extends JpaRepository<Bikers, Long>, JpaSpecificationExecutor<Bikers> {

    Optional<Bikers> findByBikerId(Long bikerId);

    boolean existsById(Bikers bikerId);

    @Query("SELECT b FROM Bikers b WHERE b.isOnline = true AND b.isAvailable = true AND b.isActive = true")
    List<Bikers> findAvailableBikers();

    Optional<Bikers> findByEmail(String email);

    Optional<Bikers> findByPhoneNumber(String phoneNumber);

    Optional<Bikers> findByLicenseNumber(String licenseNumber);

    Optional<Bikers> findByIsActiveTrue();

    @Query("SELECT b FROM Bikers b WHERE " +
            "(:isActive IS NULL OR b.isActive = :isActive) AND " +
            "(:isOnline IS NULL OR b.isOnline = :isOnline) AND " +
            "(:isAvailable IS NULL OR b.isAvailable = :isAvailable) " +
            "ORDER BY b.createdAt DESC")
    Page<Bikers> findAllWithFilters(
            @Param("isActive") Boolean isActive,
            @Param("isOnline") Boolean isOnline,
            @Param("isAvailable") Boolean isAvailable,
            Pageable pageable);

    // ============ Dashboard Aggregation Queries ============

    // Count bikers by status
    Long countByIsActiveTrue();

    Long countByIsOnlineTrue();

    Long countByIsAvailableTrue();

    // Get average biker rating
    @Query("SELECT AVG(b.rating) FROM Bikers b WHERE b.isActive = true")
    Double getAverageBikerRating();

    // Get top performing bikers
    @Query("SELECT b FROM Bikers b WHERE b.isActive = true ORDER BY b.rating DESC")
    List<Bikers> findTopRatedBikers(Pageable pageable);

}
