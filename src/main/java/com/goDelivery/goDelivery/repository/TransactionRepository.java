package com.goDelivery.goDelivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.goDelivery.goDelivery.model.Transaction;
import com.goDelivery.goDelivery.Enum.TransactionStatus;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReferenceId(String referenceId);
    Optional<Transaction> findByExternalId(String externalId);
    List<Transaction> findByStatus(TransactionStatus status);
    boolean existsByExternalId(String externalId);
    

    
}
