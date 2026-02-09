package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.agreement.AcceptAgreementRequest;
import com.goDelivery.goDelivery.dtos.agreement.SystemDeliveryAgreementDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.RestaurantDeliveryAgreement;
import com.goDelivery.goDelivery.model.SystemDeliveryAgreement;
import com.goDelivery.goDelivery.repository.RestaurantDeliveryAgreementRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import com.goDelivery.goDelivery.repository.SystemDeliveryAgreementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemDeliveryAgreementService {

    private final SystemDeliveryAgreementRepository agreementRepository;
    private final RestaurantDeliveryAgreementRepository restaurantAgreementRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Get the current active agreement
     */
    public SystemDeliveryAgreementDTO getCurrentAgreement() {
        SystemDeliveryAgreement agreement = agreementRepository
                .findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No active agreement found"));

        return mapToDTO(agreement);
    }

    /**
     * Accept the system delivery agreement for a restaurant
     */
    @Transactional
    public void acceptAgreement(Long restaurantId, AcceptAgreementRequest request, String ipAddress) {
        if (!request.getAccepted()) {
            throw new IllegalArgumentException("Agreement must be accepted to proceed");
        }

        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        SystemDeliveryAgreement agreement = agreementRepository
                .findByVersionAndIsActiveTrue(request.getAgreementVersion())
                .orElseThrow(() -> new ResourceNotFoundException("Agreement version not found or inactive"));

        // Check if already accepted
        if (restaurantAgreementRepository.existsByRestaurant_RestaurantId(restaurantId)) {
            log.warn("Restaurant {} has already accepted an agreement", restaurantId);
            // Allow re-acceptance for new versions
        }

        // Create agreement record
        RestaurantDeliveryAgreement restaurantAgreement = RestaurantDeliveryAgreement.builder()
                .restaurant(restaurant)
                .agreement(agreement)
                .acceptedAt(LocalDateTime.now())
                .acceptedBy(restaurant.getEmail())
                .ipAddress(ipAddress)
                .build();

        restaurantAgreementRepository.save(restaurantAgreement);

        // Update restaurant
        restaurant.setSystemDeliveryAgreementAccepted(true);
        restaurant.setSystemDeliveryAgreementDate(LocalDateTime.now());
        restaurant.setSystemDeliveryAgreementVersion(agreement.getVersion());
        restaurantRepository.save(restaurant);

        log.info("Restaurant {} accepted system delivery agreement version {} from IP {}",
                restaurantId, agreement.getVersion(), ipAddress);
    }

    /**
     * Check if restaurant has accepted the agreement
     */
    public boolean hasAcceptedAgreement(Long restaurantId) {
        return restaurantAgreementRepository.existsByRestaurant_RestaurantId(restaurantId);
    }

    /**
     * Get restaurant's agreement details
     */
    public RestaurantDeliveryAgreement getRestaurantAgreement(Long restaurantId) {
        return restaurantAgreementRepository.findByRestaurant_RestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("No agreement found for restaurant"));
    }

    /**
     * Map entity to DTO
     */
    private SystemDeliveryAgreementDTO mapToDTO(SystemDeliveryAgreement agreement) {
        return SystemDeliveryAgreementDTO.builder()
                .id(agreement.getId())
                .version(agreement.getVersion())
                .agreementText(agreement.getAgreementText())
                .terms(agreement.getTerms())
                .commissionPercentage(agreement.getCommissionPercentage())
                .createdAt(agreement.getCreatedAt())
                .build();
    }
}
