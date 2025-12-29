package com.goDelivery.goDelivery.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.goDelivery.goDelivery.Enum.DisbursementStatus;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.config.MomoConfig;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.CollectionDisbursementRequest;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.CollectionDisbursementResponse;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementCallback;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementRecipient;
import com.goDelivery.goDelivery.exception.PaymentProcessingException;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.DisbursementTransaction;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.DisbursementTransactionRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.repository.PaymentRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisbursementService {
    private final MomoService momoService;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final DisbursementTransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final MomoConfig momoConfig;
    private final NotificationService notificationService;

    @Value("${app.payment.method.momo:MoMo}")
    private String momoPaymentMethod;


    @Transactional
    public CollectionDisbursementResponse processOrderDisbursement(Order order) {
        // Validate order
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Order is not paid");
        }

        // Get all unique restaurants from order items
        Map<Restaurant, Double> restaurantAmounts = calculateRestaurantAmounts(order);
        
        // Prepare disbursement recipients
        List<DisbursementRecipient> recipients = new ArrayList<>();
        int index = 1;
        
        for (Map.Entry<Restaurant, Double> entry : restaurantAmounts.entrySet()) {
            Restaurant restaurant = entry.getKey();
            Double amount = entry.getValue();
            
            if (restaurant.getPhoneNumber() == null) {
                throw new IllegalStateException(
                    String.format("Restaurant %s doesn't have a registered MoMo number", 
                        restaurant.getRestaurantName()));
            }
            
            // Calculate amount after commission
            double commission = calculateCommission(amount);
            double amountToDisburse = amount - commission;
            
            // Create recipient
            DisbursementRecipient recipient = DisbursementRecipient.builder()
                .externalId(String.format("DISP_%s_%02d", order.getOrderNumber(), index++))
                .msisdn(restaurant.getPhoneNumber())
                .amount(amountToDisburse)
                .payerMessageTitle("Payment from MozFood")
                .payerMessageDescription(String.format("Payment for order #%s", order.getOrderNumber()))
                .build();
                
            recipients.add(recipient);
            
            // Create disbursement transaction record
            DisbursementTransaction transaction = DisbursementTransaction.builder()
                .order(order)
                .restaurant(restaurant)
                .amount(amountToDisburse)
                .commission(commission)
                .referenceId(recipient.getExternalId())
                .status(DisbursementStatus.PENDING)
                .build();
                
            transactionRepository.save(transaction);
        }
        
        // Prepare and send collection-disbursement request
        CollectionDisbursementRequest request = CollectionDisbursementRequest.builder()
            .collectionExternalId("COLL_" + order.getOrderNumber())
            .collectionMsisdn(order.getCustomer().getPhoneNumber())
            .collectionAmount(restaurantAmounts.values().stream().mapToDouble(Double::doubleValue).sum())
            .collectionPayerMessageTitle("MozFood Order #" + order.getOrderNumber())
            .collectionPayerMessageDescription("Payment for your order")
            .callback(momoConfig.getCollectionDisbursementUrl() + "/api/webhooks/momo/disbursement")
            .disbursementRecipients(recipients)
            .build();
            
        // Send request to MoMo
        CollectionDisbursementResponse response = momoService.initiateCollectionDisbursement(request);
        
        // Update order and transactions with the reference ID
        order.setDisbursementReference(response.getReferenceId());
        orderRepository.save(order);
        
        transactionRepository.updateReferenceIdByOrder(
            response.getReferenceId(),
            order.getOrderId()
        );
        
        return response;
    }

    private Map<Restaurant, Double> calculateRestaurantAmounts(Order order) {
        // Group order items by restaurant and calculate total amount for each
        return order.getOrderItems().stream()
            .collect(Collectors.groupingBy(
                item -> item.getMenuItem().getRestaurant(),
                Collectors.summingDouble(item -> 
                    item.getUnitPrice() * item.getQuantity()
                )
            ));
    }

    private double calculateCommission(double amount) {
        // Example: 15% commission
        return amount * 0.15;
    }

    @Transactional
    public void handleDisbursementCallback(DisbursementCallback callback) {
        // Handle webhook callback from MoMo
        switch (callback.getType()) {
            case COLLECTION:
                handleCollectionCallback(callback);
                break;
            case DISBURSEMENT:
                handleDisbursementCallback(callback);
                break;
            default:
                log.warn("Unknown callback type: {}", callback.getType());
        }
    }

    private void handleCollectionCallback(DisbursementCallback callback) {
        // Handle collection status update
        Order order = orderRepository.findByDisbursementReference(callback.getReferenceId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Order not found for reference: " + callback.getReferenceId()));
                
        if (DisbursementStatus.SUCCESSFUL.equals(callback.getStatus())) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentCompletedAt(LocalDateTime.now());
        } else if (DisbursementStatus.FAILED.equals(callback.getStatus())) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setPaymentFailureReason(callback.getErrorReason());
        }
        
        orderRepository.save(order);
    }

    private void handleDisbursementStatusUpdate(DisbursementCallback callback) {
        try {
            // Handle disbursement status update
            DisbursementTransaction transaction = transactionRepository
                .findByReferenceId(callback.getReferenceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Transaction not found for reference: " + callback.getReferenceId()));
            
            // Update transaction status
            transaction.setStatus(callback.getStatus());
            
            if (callback.getFinancialTransactionId() != null) {
                transaction.setFinancialTransactionId(callback.getFinancialTransactionId());
            }
            
            if (callback.getErrorReason() != null) {
                transaction.setErrorMessage(momoPaymentMethod + " " + callback.getErrorReason());
            }
            
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            // If disbursement was successful, send notification to the restaurant
            if (DisbursementStatus.SUCCESSFUL.equals(callback.getStatus())) {
                Order order = orderRepository.findByOrderId(transaction.getOrder().getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found for ID: " + transaction.getOrder().getOrderId()));
                
                // Send payment notification to the restaurant
                notificationService.sendPaymentNotification(
                    order.getRestaurant().getRestaurantId(),
                    transaction.getAmount().doubleValue(),
                    order.getOrderNumber(),
                    momoPaymentMethod
                );
                
                log.info("Sent payment notification to restaurant {} for order {}",
                    order.getRestaurant().getRestaurantId(), order.getOrderNumber());
            }
            
        } catch (Exception e) {
            log.error("Error processing disbursement callback: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Error processing disbursement callback", e);
        }
    }

    private void checkAndUpdateOrderDisbursementStatus(Order order) {
        List<DisbursementTransaction> transactions = transactionRepository
            .findAllByOrder(order);
            
        boolean allSuccessful = transactions.stream()
            .allMatch(t -> t.getStatus() == DisbursementStatus.SUCCESSFUL);
            
        boolean anyFailed = transactions.stream()
            .anyMatch(t -> t.getStatus() == DisbursementStatus.FAILED);
            
        if (allSuccessful) {
            order.setDisbursementStatus(DisbursementStatus.SUCCESSFUL);
            order.setDisbursementCompletedAt(LocalDateTime.now());
        } else if (anyFailed) {
            order.setDisbursementStatus(DisbursementStatus.FAILED);
        }
        
        orderRepository.save(order);
    }
}
