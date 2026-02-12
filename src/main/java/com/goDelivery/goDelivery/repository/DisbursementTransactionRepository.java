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

       @Modifying
       @Query("UPDATE DisbursementTransaction t SET t.referenceId = :referenceId WHERE t.order.id IN :orderIds")
       void updateReferenceIdByOrderIn(@Param("referenceId") String referenceId,
                     @Param("orderIds") List<Long> orderIds);

       // Temporarily commented out due to constructor resolution issues
       /*
        * @Query("SELECT new com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementSummaryDTO("
        * +
        * "dt.id, dt.referenceId, dt.order.orderId, dt.order.orderNumber, dt.restaurant.id, dt.restaurant.restaurantName, "
        * +
        * "CAST(dt.amount AS java.math.BigDecimal), CAST(dt.commission AS java.math.BigDecimal), "
        * +
        * "dt.status, dt.createdAt, dt.updatedAt) " +
        * "FROM DisbursementTransaction dt " +
        * "WHERE dt.restaurant.id = :restaurantId " +
        * "ORDER BY dt.createdAt DESC")
        * List<DisbursementSummaryDTO>
        * findDisbursementSummaryByRestaurantId(@Param("restaurantId") Long
        * restaurantId);
        * 
        * @Query("SELECT new com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementSummaryDTO("
        * +
        * "dt.id, dt.referenceId, dt.order.orderId, dt.order.orderNumber, dt.restaurant.id, dt.restaurant.restaurantName, "
        * +
        * "CAST(dt.amount AS java.math.BigDecimal), CAST(dt.commission AS java.math.BigDecimal), "
        * +
        * "dt.status, dt.createdAt, dt.updatedAt) " +
        * "FROM DisbursementTransaction dt " +
        * "ORDER BY dt.createdAt DESC")
        * List<DisbursementSummaryDTO> findAllDisbursementSummaries();
        * 
        * @Query("SELECT NEW com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.RestaurantDisbursementSummaryDTO("
        * +
        * "r.id, r.restaurantName, " +
        * "COALESCE(CAST(SUM(dt.amount) AS java.math.BigDecimal), 0), " +
        * "COALESCE(CAST(SUM(dt.commission) AS java.math.BigDecimal), 0), " +
        * "CAST(COUNT(dt) AS java.lang.Long)) " +
        * "FROM DisbursementTransaction dt " +
        * "JOIN dt.restaurant r " +
        * "GROUP BY r.id, r.restaurantName")
        * List<RestaurantDisbursementSummaryDTO> getRestaurantDisbursementSummaries();
        */

}