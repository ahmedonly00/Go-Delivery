package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.tracking.BikerInfo;
import com.goDelivery.goDelivery.dtos.tracking.BikerLocationUpdateRequest;
import com.goDelivery.goDelivery.dtos.tracking.DeliveryTrackingResponse;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.Bikers;
import com.goDelivery.goDelivery.model.DeliveryTracking;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.repository.DeliveryTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryTrackingService {

        private final DeliveryTrackingRepository trackingRepository;
        private final SimpMessagingTemplate messagingTemplate;
        private final DistanceCalculationService distanceService;

        /**
         * Update biker location and send real-time update via WebSocket
         */
        @Transactional
        public void updateBikerLocation(BikerLocationUpdateRequest request) {
                DeliveryTracking tracking = trackingRepository
                                .findByOrder_OrderId(request.getOrderId())
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Tracking not found for order: "
                                                                + request.getOrderId()));

                // Update location
                tracking.setLatitude(request.getLatitude());
                tracking.setLongitude(request.getLongitude());
                tracking.setUpdatedAt(LocalDateTime.now());

                // Update status if provided
                if (request.getStatus() != null) {
                        tracking.setDeliveryStatus(request.getStatus());
                }

                // Update status message if provided
                if (request.getStatusMessage() != null && !request.getStatusMessage().trim().isEmpty()) {
                        tracking.setCurrentStatusMessage(request.getStatusMessage());
                }

                // Note: Distance and ETA calculation would require Order to have delivery
                // coordinates
                // For now, we'll skip this and just update the location
                // TODO: Add deliveryLatitude and deliveryLongitude to Order model for full
                // tracking

                trackingRepository.save(tracking);

                // Send WebSocket update to customer
                DeliveryTrackingResponse response = buildTrackingResponse(tracking);
                messagingTemplate.convertAndSend(
                                "/topic/delivery/" + tracking.getOrder().getOrderId(),
                                response);

                log.info("Updated tracking for order {}: {} at ({}, {})",
                                tracking.getOrder().getOrderId(),
                                request.getStatusMessage() != null ? request.getStatusMessage() : "Location update",
                                request.getLatitude(), request.getLongitude());
        }

        /**
         * Get current tracking information for an order
         */
        public DeliveryTrackingResponse getOrderTracking(Long orderId) {
                DeliveryTracking tracking = trackingRepository
                                .findByOrder_OrderId(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tracking not found for order: " + orderId));

                return buildTrackingResponse(tracking);
        }

        /**
         * Build tracking response DTO from entity
         */
        private DeliveryTrackingResponse buildTrackingResponse(DeliveryTracking tracking) {
                Bikers biker = tracking.getBikers();

                BikerInfo bikerInfo = BikerInfo.builder()
                                .bikerId(biker.getBikerId())
                                .name(biker.getFullName())
                                .phoneNumber(biker.getPhoneNumber())
                                .build();

                return DeliveryTrackingResponse.builder()
                                .trackingId(tracking.getTrackingId())
                                .orderId(tracking.getOrder().getOrderId())
                                .status(tracking.getDeliveryStatus())
                                .currentLatitude(tracking.getLatitude())
                                .currentLongitude(tracking.getLongitude())
                                .statusMessage(tracking.getCurrentStatusMessage())
                                .estimatedArrivalTime(tracking.getEstimatedArrivalTime())
                                .distanceToDestinationKm(tracking.getDistanceToDestinationKm())
                                .bikerInfo(bikerInfo)
                                .lastUpdated(tracking.getUpdatedAt())
                                .build();
        }
}
