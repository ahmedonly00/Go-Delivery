package com.goDelivery.goDelivery.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.Enum.DisbursementStatus;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementSummaryDTO;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.RestaurantDisbursementSummaryDTO;
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

    @Query("SELECT new com.goDelivery.goDelivery.dto.DisbursementSummaryDTO(" +
           "dt.id, dt.referenceId, o.orderId, o.orderNumber, r.id, r.restaurantName, " +
           "dt.amount, dt.commission, dt.status, dt.createdAt, dt.updatedAt) " +
           "FROM DisbursementTransaction dt " +
           "JOIN dt.order o " +
           "JOIN dt.restaurant r " +
           "WHERE r.id = :restaurantId " +
           "ORDER BY dt.createdAt DESC")
    List<DisbursementSummaryDTO> findDisbursementSummaryByRestaurantId(@Param("restaurantId") Long restaurantId);
    @Query("SELECT new com.goDelivery.goDelivery.dto.DisbursementSummaryDTO(" +
           "dt.id, dt.referenceId, o.orderId, o.orderNumber, r.id, r.restaurantName, " +
           "dt.amount, dt.commission, dt.status, dt.createdAt, dt.updatedAt) " +
           "FROM DisbursementTransaction dt " +
           "JOIN dt.order o " +
           "JOIN dt.restaurant r " +
           "ORDER BY dt.createdAt DESC")
    List<DisbursementSummaryDTO> findAllDisbursementSummaries();
    @Query("SELECT NEW com.goDelivery.goDelivery.dto.RestaurantDisbursementSummaryDTO(" +
           "r.id, r.restaurantName, " +
           "COALESCE(SUM(dt.amount), 0), " +
           "COALESCE(SUM(dt.commission), 0), " +
           "COUNT(dt)) " +
           "FROM DisbursementTransaction dt " +
           "JOIN dt.restaurant r " +
           "GROUP BY r.id, r.restaurantName")
    List<RestaurantDisbursementSummaryDTO> getRestaurantDisbursementSummaries();
    
}