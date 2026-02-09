package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.SystemDeliveryAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemDeliveryAgreementRepository extends JpaRepository<SystemDeliveryAgreement, Long> {

    Optional<SystemDeliveryAgreement> findByVersionAndIsActiveTrue(String version);

    Optional<SystemDeliveryAgreement> findFirstByIsActiveTrueOrderByCreatedAtDesc();
}
