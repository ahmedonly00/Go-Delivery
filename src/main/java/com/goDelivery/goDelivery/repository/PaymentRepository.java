package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_OrderId(Long orderId);
    
    /**
     * Find a payment by its transaction ID
     * @param transactionId The transaction ID to search for
     * @return An Optional containing the payment if found, or empty if not found
     */
    Optional<Payment> findByTransactionId(String transactionId);
}
