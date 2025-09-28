package com.goDelivery.goDelivery.repository;

import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.Customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerId(Long customerId);

    Optional<Customer> findByEmail(String email);    
}
