package com.goDelivery.goDelivery.modules.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.goDelivery.goDelivery.modules.payment.model.Transaction;
import com.goDelivery.goDelivery.shared.enums.TransactionStatus;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReferenceId(String referenceId);
    Optional<Transaction> findByExternalId(String externalId);
    List<Transaction> findByStatus(TransactionStatus status);
    boolean existsByExternalId(String externalId);
    

    
}
