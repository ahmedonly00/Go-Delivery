package com.goDelivery.goDelivery.repository;

import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.Customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerId(Long customerId);

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    // ============ Dashboard Aggregation Queries ============

    // Count customers by date range
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    Long countCustomersByDateRange(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);
}
