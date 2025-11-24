package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.MomoTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MomoTransactionRepository extends JpaRepository<MomoTransaction, Long> {
    Optional<MomoTransaction> findByReferenceId(String referenceId);
    Optional<MomoTransaction> findByExternalId(String externalId);
    boolean existsByExternalId(String externalId);
}
