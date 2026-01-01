package com.goDelivery.goDelivery.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.Enum.DisbursementStatus;
import com.goDelivery.goDelivery.model.DisbursementTransaction;
import com.goDelivery.goDelivery.model.Order;


@Repository
public interface DisbursementTransactionRepository extends JpaRepository<DisbursementTransaction, Long> {
    List<DisbursementTransaction> findAllByOrder(Order order);
    List<DisbursementTransaction> findByOrder_OrderId(Long orderId);
    List<DisbursementTransaction> findByStatus(DisbursementStatus status);
    Optional<DisbursementTransaction> findByReferenceId(String referenceId);
    
    @Modifying
    @Query("UPDATE DisbursementTransaction t SET t.referenceId = :referenceId WHERE t.order.id = :orderId")
    void updateReferenceIdByOrder(@Param("referenceId") String referenceId, @Param("orderId") Long orderId);
    
}