package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.model.MpesaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MpesaTransactionRepository extends JpaRepository<MpesaTransaction, Long> {
    Optional<MpesaTransaction> findByTransactionId(String transactionId);
    Optional<MpesaTransaction> findByThirdPartyRef(String thirdPartyRef);
    boolean existsByTransactionId(String transactionId);
    boolean existsByThirdPartyRef(String thirdPartyRef);
    long countByStatus(PaymentStatus status);
}
