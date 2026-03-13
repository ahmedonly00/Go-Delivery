package com.goDelivery.goDelivery.modules.delivery.repository;

import com.goDelivery.goDelivery.modules.delivery.model.DeliveryTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Long> {

    Optional<DeliveryTracking> findByOrder_OrderId(Long orderId);
}
