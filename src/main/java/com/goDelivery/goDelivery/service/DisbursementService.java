package com.goDelivery.goDelivery.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goDelivery.goDelivery.Enum.DisbursementStatus;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.config.CommissionConfig;
import com.goDelivery.goDelivery.config.MomoConfig;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.CollectionDisbursementRequest;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.CollectionDisbursementResponse;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementCallback;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementRecipient;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementStatusResponse;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.DisbursementSummaryDTO;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.RestaurantDisbursementSummaryDTO;
import com.goDelivery.goDelivery.exception.PaymentProcessingException;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.DisbursementTransaction;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.model.OrderItem;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.repository.DisbursementTransactionRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DisbursementService {
        @Lazy
        @Autowired
        private MomoService momoService;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private DisbursementTransactionRepository transactionRepository;

        @Autowired
        private MomoConfig momoConfig;

        @Autowired
        private CommissionConfig commissionConfig;

        @Autowired
        private NotificationService notificationService;

        @Value("${app.payment.method.momo:MoMo}")
        private String momoPaymentMethod;

        @Transactional
        public CollectionDisbursementResponse processOrderDisbursement(Order order) {
                // Validate order
                if (order.getPaymentStatus() != PaymentStatus.PAID) {
                        throw new IllegalStateException("Order is not paid");
                }

                // Extract parent order number (remove -1, -2, etc. suffix for child orders)
                String parentOrderNumber = extractParentOrderNumber(order.getOrderNumber());

                log.info("Processing disbursement for order: {}, parent order number: {}",
                                order.getOrderNumber(), parentOrderNumber);

                // Find ALL related orders with same parent number (multi-restaurant orders)
                List<Order> relatedOrders = orderRepository.findByOrderNumberStartingWith(parentOrderNumber);

                log.info("Found {} related orders for parent order number: {}",
                                relatedOrders.size(), parentOrderNumber);

                // Collect all order items from ALL related orders
                List<OrderItem> allOrderItems = relatedOrders.stream()
                                .flatMap(o -> o.getOrderItems().stream())
                                .collect(Collectors.toList());

                // Calculate restaurant amounts from ALL items
                Map<Restaurant, Double> restaurantAmounts = calculateRestaurantAmounts(allOrderItems);
                double totalAmount = restaurantAmounts.values().stream()
                                .mapToDouble(Double::doubleValue).sum();

                log.info("Disbursing to {} restaurants, total amount: {}",
                                restaurantAmounts.size(), totalAmount);

                // Prepare disbursement recipients for ALL restaurants
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
                                        .externalId(String.format("DISP_%s_%02d", parentOrderNumber, index++))
                                        .msisdn(restaurant.getPhoneNumber())
                                        .amount(amountToDisburse)
                                        .payerMessageTitle("Payment from MozFood")
                                        .payerMessageDescription(
                                                        String.format("Payment for order #%s", parentOrderNumber))
                                        .build();

                        recipients.add(recipient);

                        // Find the order for this specific restaurant
                        Order restaurantOrder = findOrderForRestaurant(relatedOrders, restaurant);

                        // Create disbursement transaction record for EACH restaurant
                        DisbursementTransaction transaction = DisbursementTransaction.builder()
                                        .order(restaurantOrder)
                                        .restaurant(restaurant)
                                        .amount(amountToDisburse)
                                        .commission(commission)
                                        .referenceId(recipient.getExternalId())
                                        .status(DisbursementStatus.PENDING)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                        transactionRepository.save(transaction);

                        log.info("Created disbursement for restaurant {} - Amount: {}, Commission: {}",
                                        restaurant.getRestaurantName(), amountToDisburse, commission);
                }

                // Prepare and send collection-disbursement request
                CollectionDisbursementRequest request = CollectionDisbursementRequest.builder()
                                .collectionExternalId("COLL_" + parentOrderNumber)
                                .collectionMsisdn(order.getCustomer().getPhoneNumber())
                                .collectionAmount(totalAmount)
                                .collectionPayerMessageTitle("MozFood Order #" + parentOrderNumber)
                                .collectionPayerMessageDescription("Payment for your order")
                                .callback(momoConfig.getCallbackHost() + "/api/webhooks/momo/disbursement")
                                .disbursementRecipients(recipients)
                                .build();

                // Send request to MoMo
                CollectionDisbursementResponse response = momoService.initiateCollectionDisbursement(request);

                // Update ALL related orders with the reference ID
                for (Order relatedOrder : relatedOrders) {
                        relatedOrder.setDisbursementReference(response.getReferenceId());
                        relatedOrder.setDisbursementStatus(DisbursementStatus.PENDING);
                        relatedOrder.setUpdatedAt(LocalDateTime.now());
                        orderRepository.save(relatedOrder);
                }

                // Update transaction reference IDs for all related orders
                List<Long> orderIds = relatedOrders.stream()
                                .map(Order::getOrderId)
                                .collect(Collectors.toList());
                transactionRepository.updateReferenceIdByOrderIn(response.getReferenceId(), orderIds);

                log.info("Successfully initiated disbursement for {} restaurants with reference: {}",
                                recipients.size(), response.getReferenceId());

                return response;
        }

        /**
         * Extract parent order number from order number.
         * Example: ORD-20260212-1234-1 -> ORD-20260212-1234
         */
        private String extractParentOrderNumber(String orderNumber) {
                int lastDashIndex = orderNumber.lastIndexOf('-');
                if (lastDashIndex > 0) {
                        String suffix = orderNumber.substring(lastDashIndex + 1);
                        // Check if suffix is a number (child order indicator)
                        if (suffix.matches("\\d+")) {
                                return orderNumber.substring(0, lastDashIndex);
                        }
                }
                return orderNumber; // Already parent order number
        }

        /**
         * Find the order for a specific restaurant from list of related orders
         */
        private Order findOrderForRestaurant(List<Order> orders, Restaurant restaurant) {
                return orders.stream()
                                .filter(o -> o.getRestaurant().getRestaurantId().equals(restaurant.getRestaurantId()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                                "No order found for restaurant: " + restaurant.getRestaurantName()));
        }

        /**
         * Calculate restaurant amounts from a list of order items
         */
        private Map<Restaurant, Double> calculateRestaurantAmounts(List<OrderItem> orderItems) {
                // Group order items by restaurant and calculate total amount for each
                return orderItems.stream()
                                .collect(Collectors.groupingBy(
                                                item -> item.getMenuItem().getRestaurant(),
                                                Collectors.summingDouble(
                                                                item -> item.getUnitPrice() * item.getQuantity())));
        }

        private double calculateCommission(double amount) {
                // Use configured commission rate
                double commission = amount * commissionConfig.getDefaultRate();

                // Apply minimum and maximum limits
                commission = Math.max(commission, commissionConfig.getMinimumAmount());
                commission = Math.min(commission, commissionConfig.getMaximumAmount());

                // Check for tiered rates (if configured)
                if (commissionConfig.getTieredRates() != null) {
                        for (CommissionConfig.TieredRate tier : commissionConfig.getTieredRates()) {
                                if (amount >= tier.getMinAmount() && amount < tier.getMaxAmount()) {
                                        commission = amount * tier.getRate();
                                        break;
                                }
                        }
                }

                log.debug("Calculated commission: {} for amount: {}", commission, amount);
                return commission;
        }

        @Transactional
        public void handleDisbursementCallback(DisbursementCallback callback) {
                log.info("Received disbursement callback: {}", callback);

                // Handle webhook callback from MoMo
                switch (callback.getType()) {
                        case COLLECTION:
                                handleCollectionCallback(callback);
                                break;
                        case DISBURSEMENT:
                                handleDisbursementStatusUpdate(callback);
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

                log.info("Processing collection callback for order {}: {}",
                                order.getOrderId(), callback.getStatus());

                if (DisbursementStatus.SUCCESSFUL.equals(callback.getStatus())) {
                        order.setPaymentStatus(PaymentStatus.PAID);
                        order.setPaymentCompletedAt(LocalDateTime.now());
                        order.setUpdatedAt(LocalDateTime.now());
                        log.info("Collection successful for order {}", order.getOrderId());
                } else if (DisbursementStatus.FAILED.equals(callback.getStatus())) {
                        order.setPaymentStatus(PaymentStatus.FAILED);
                        order.setPaymentFailureReason(callback.getErrorReason());
                        order.setUpdatedAt(LocalDateTime.now());
                        log.info("Collection failed for order {}", order.getOrderId());
                }

                orderRepository.save(order);
        }

        private void handleDisbursementStatusUpdate(DisbursementCallback callback) {
                try {

                        log.info("Processing disbursement callback for reference: {}", callback.getReferenceId());

                        // Handle disbursement status update
                        DisbursementTransaction transaction = transactionRepository
                                        .findByReferenceId(callback.getReferenceId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Transaction not found for reference: "
                                                                        + callback.getReferenceId()));

                        log.info("Processing disbursement callback for transaction: {}", transaction.getReferenceId());

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

                        log.info("Updated transaction {} to status: {}",
                                        transaction.getReferenceId(), callback.getStatus());

                        // If disbursement was successful, send notification to the restaurant
                        if (DisbursementStatus.SUCCESSFUL.equals(callback.getStatus())) {
                                Order order = orderRepository.findByOrderId(transaction.getOrder().getOrderId())
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Order not found for ID: "
                                                                                + transaction.getOrder().getOrderId()));

                                log.info("Sending payment notification to restaurant {} for order {}",
                                                order.getRestaurant().getRestaurantId(), order.getOrderNumber());

                                // Send payment notification to the restaurant
                                notificationService.sendPaymentNotification(
                                                order.getRestaurant().getRestaurantId(),
                                                transaction.getAmount().doubleValue(),
                                                order.getOrderNumber(),
                                                momoPaymentMethod);

                                log.info("Sent payment notification to restaurant {} for order {}",
                                                order.getRestaurant().getRestaurantId(), order.getOrderNumber());
                        }

                        log.info("Disbursement callback processed successfully for transaction: {}",
                                        transaction.getReferenceId());

                        // Update the order status if this is the last pending disbursement
                        if (transaction.getOrder() != null) {
                                checkAndUpdateOrderDisbursementStatus(transaction.getOrder());
                        }

                } catch (Exception e) {
                        log.error("Error processing disbursement callback: {}", e.getMessage(), e);
                        throw new PaymentProcessingException("Error processing disbursement callback", e);
                }
        }

        private void checkAndUpdateOrderDisbursementStatus(Order order) {
                List<DisbursementTransaction> transactions = transactionRepository
                                .findAllByOrder(order);

                if (transactions.isEmpty()) {
                        log.warn("No disbursement transactions found for order {}", order.getOrderId());
                        return;
                }

                boolean allSuccessful = transactions.stream()
                                .allMatch(t -> t.getStatus() == DisbursementStatus.SUCCESSFUL);

                boolean anyFailed = transactions.stream()
                                .anyMatch(t -> t.getStatus() == DisbursementStatus.FAILED);

                boolean anyPending = transactions.stream()
                                .anyMatch(t -> t.getStatus() == DisbursementStatus.PENDING);

                if (allSuccessful) {
                        order.setDisbursementStatus(DisbursementStatus.SUCCESSFUL);
                        order.setDisbursementCompletedAt(LocalDateTime.now());
                        log.info("All disbursements completed successfully for order {}", order.getOrderId());

                } else if (anyFailed) {
                        order.setDisbursementStatus(DisbursementStatus.FAILED);
                        log.info("Disbursement failed for order {}", order.getOrderId());
                } else if (anyPending) {
                        order.setDisbursementStatus(DisbursementStatus.PENDING);
                        log.info("Disbursement pending for order {}", order.getOrderId());
                }

                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
        }

        @Transactional(readOnly = true)
        @Cacheable(value = "disbursementStatus", key = "#referenceId")
        public Map<String, Object> getDisbursementStatus(String referenceId) {
                log.info("Fetching disbursement status for reference: {}", referenceId);

                DisbursementTransaction transaction = transactionRepository.findByReferenceId(referenceId)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "Disbursement not found with reference: "
                                                                                + referenceId));

                return Map.of(
                                "referenceId", transaction.getReferenceId(),
                                "status", transaction.getStatus().name(),
                                "amount", transaction.getAmount(),
                                "currency", "RWF", // Assuming RWF as default currency
                                "financialTransactionId",
                                transaction.getFinancialTransactionId() != null
                                                ? transaction.getFinancialTransactionId()
                                                : "N/A",
                                "externalId", "DISB" + transaction.getFinancialTransactionId(),
                                "reason", transaction.getStatus().name(),
                                "errorReason", transaction.getErrorMessage());
        }

        @Transactional(readOnly = true)
        public Map<String, Object> getCollectionStatus(String referenceId) {
                log.info("Fetching collection status for reference: {}", referenceId);

                // Find the order with this collection reference
                Order order = orderRepository.findByDisbursementReference(referenceId)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "Collection not found with reference: " + referenceId));

                // Get all transactions for this order
                List<DisbursementTransaction> transactions = transactionRepository
                                .findByOrder_OrderId(order.getOrderId());

                // Get overall status (worst case scenario)
                String overallStatus = transactions.stream()
                                .map(t -> t.getStatus())
                                .min(Comparator.naturalOrder()) // Assuming FAILED < PENDING < SUCCESSFUL
                                .orElse(DisbursementStatus.FAILED)
                                .name();

                return Map.of(
                                "referenceId", referenceId,
                                "status", overallStatus,
                                "amount", transactions.stream().mapToDouble(t -> t.getAmount().doubleValue()).sum(),
                                "currency", "RWF",
                                "financialTransactionId",
                                order.getPayment() != null && order.getPayment().getMomoTransaction() != null
                                                ? order.getPayment().getMomoTransaction().getFinancialTransactionId()
                                                : "N/A",
                                "externalId", "COLL" + order.getOrderNumber(),
                                "reason", "Order #" + order.getOrderNumber(),
                                "errorReason", transactions.stream()
                                                .filter(t -> t.getErrorMessage() != null)
                                                .map(DisbursementTransaction::getErrorMessage)
                                                .collect(Collectors.joining("; ")));
        }

        @Scheduled(fixedRate = 300000) // Every 5 minutes
        @Transactional
        public void updatePendingDisbursements() {
                log.info("Starting scheduled task to update pending disbursements");

                // Find all pending disbursement transactions
                List<DisbursementTransaction> pendingTransactions = transactionRepository
                                .findByStatus(DisbursementStatus.PENDING);

                if (pendingTransactions.isEmpty()) {
                        log.info("No pending disbursement transactions found");
                        return;
                }

                log.info("Found {} pending disbursement transactions to update", pendingTransactions.size());

                for (DisbursementTransaction transaction : pendingTransactions) {
                        try {
                                log.debug("Checking status for transaction ID: {}, Reference: {}",
                                                transaction.getFinancialTransactionId(), transaction.getReferenceId());

                                // Get the latest status from MoMo API
                                DisbursementStatusResponse statusResponse = momoService
                                                .checkDisbursementStatus(transaction.getReferenceId());
                                String status = statusResponse.getStatus();

                                log.info("Transaction {} status from provider: {}",
                                                transaction.getReferenceId(), status);

                                // Update transaction status based on provider response
                                DisbursementStatus newStatus = DisbursementStatus.valueOf(status.toUpperCase());
                                transaction.setStatus(newStatus);

                                // Update additional fields if available
                                if (statusResponse.getFinancialTransactionId() != null) {
                                        transaction.setFinancialTransactionId(
                                                        statusResponse.getFinancialTransactionId());
                                }

                                if (statusResponse.getErrorReason() != null) {
                                        transaction.setErrorMessage(statusResponse.getErrorReason());
                                }

                                transaction.setUpdatedAt(LocalDateTime.now());

                                // Save the updated transaction
                                transaction = transactionRepository.save(transaction);
                                log.info("Updated transaction {} to status: {}",
                                                transaction.getReferenceId(), newStatus);

                                // If this was a collection, update the order status
                                if (transaction.getOrder() != null) {
                                        checkAndUpdateOrderDisbursementStatus(transaction.getOrder());
                                }

                        } catch (Exception e) {
                                log.error("Error updating status for transaction {}: {}",
                                                transaction.getReferenceId(), e.getMessage(), e);
                        }
                }
        }

        @Transactional(readOnly = true)
        public List<DisbursementSummaryDTO> getDisbursementSummaryByRestaurantId(Long restaurantId) {
                // Alternative implementation since JPQL constructor query is temporarily
                // disabled
                List<DisbursementTransaction> transactions = transactionRepository.findAll();
                return transactions.stream()
                                .filter(dt -> dt.getRestaurant().getRestaurantId().equals(restaurantId))
                                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                                .map(this::convertToDisbursementSummaryDTO)
                                .collect(Collectors.toList());
        }

        // Method name expected by controller
        public List<DisbursementSummaryDTO> getDisbursementsForRestaurant(Long restaurantId) {
                return getDisbursementSummaryByRestaurantId(restaurantId);
        }

        public List<DisbursementSummaryDTO> getAllDisbursementSummaries() {
                // Alternative implementation since JPQL constructor query is temporarily
                // disabled
                return transactionRepository.findAll().stream()
                                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                                .map(this::convertToDisbursementSummaryDTO)
                                .collect(Collectors.toList());
        }

        // Method name expected by controller
        public List<DisbursementSummaryDTO> getAllDisbursements() {
                return getAllDisbursementSummaries();
        }

        public List<RestaurantDisbursementSummaryDTO> getRestaurantDisbursementSummaries() {
                // Alternative implementation since JPQL constructor query is temporarily
                // disabled
                List<DisbursementTransaction> transactions = transactionRepository.findAll();
                return transactions.stream()
                                .collect(Collectors.groupingBy(
                                                DisbursementTransaction::getRestaurant,
                                                Collectors.toList()))
                                .entrySet().stream()
                                .map(entry -> convertToRestaurantSummaryDTO(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList());
        }

        private DisbursementSummaryDTO convertToDisbursementSummaryDTO(DisbursementTransaction transaction) {
                return DisbursementSummaryDTO.builder()
                                .transactionId(transaction.getId())
                                .referenceId(transaction.getReferenceId())
                                .orderId(transaction.getOrder().getOrderId())
                                .orderNumber(transaction.getOrder().getOrderNumber())
                                .restaurantId(transaction.getRestaurant().getRestaurantId())
                                .restaurantName(transaction.getRestaurant().getRestaurantName())
                                .amount(BigDecimal.valueOf(transaction.getAmount()))
                                .commission(BigDecimal.valueOf(transaction.getCommission()))
                                .status(transaction.getStatus().name())
                                .createdAt(transaction.getCreatedAt())
                                .updatedAt(transaction.getUpdatedAt())
                                .build();
        }

        private RestaurantDisbursementSummaryDTO convertToRestaurantSummaryDTO(Restaurant restaurant,
                        List<DisbursementTransaction> transactions) {
                List<DisbursementSummaryDTO> dtos = transactions.stream()
                                .map(this::convertToDisbursementSummaryDTO)
                                .collect(Collectors.toList());

                BigDecimal totalDisbursed = dtos.stream()
                                .map(DisbursementSummaryDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalCommission = dtos.stream()
                                .map(DisbursementSummaryDTO::getCommission)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new RestaurantDisbursementSummaryDTO(
                                restaurant.getRestaurantId(),
                                restaurant.getRestaurantName(),
                                totalDisbursed,
                                totalCommission,
                                (long) transactions.size(),
                                dtos);
        }

        @Transactional(readOnly = true)
        @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
        public List<RestaurantDisbursementSummaryDTO> getAdminRestaurantDisbursementSummaries() {
                // Alternative implementation since JPQL constructor query is temporarily
                // disabled
                List<DisbursementTransaction> transactions = transactionRepository.findAll();
                return transactions.stream()
                                .collect(Collectors.groupingBy(
                                                DisbursementTransaction::getRestaurant,
                                                Collectors.toList()))
                                .entrySet().stream()
                                .map(entry -> convertToRestaurantSummaryDTO(entry.getKey(), entry.getValue()))
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public RestaurantDisbursementSummaryDTO getRestaurantDisbursementSummary(Long restaurantId) {
                // Alternative implementation since JPQL constructor query is temporarily
                // disabled
                List<DisbursementTransaction> transactions = transactionRepository.findAll();
                List<DisbursementTransaction> filteredTransactions = transactions.stream()
                                .filter(dt -> dt.getRestaurant().getRestaurantId().equals(restaurantId))
                                .collect(Collectors.toList());

                if (filteredTransactions.isEmpty()) {
                        return new RestaurantDisbursementSummaryDTO(
                                        restaurantId,
                                        "", // Restaurant name will be empty if no transactions
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        0L,
                                        new ArrayList<>());
                }

                List<DisbursementSummaryDTO> dtos = filteredTransactions.stream()
                                .map(this::convertToDisbursementSummaryDTO)
                                .collect(Collectors.toList());

                BigDecimal totalDisbursed = dtos.stream()
                                .map(DisbursementSummaryDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalCommission = dtos.stream()
                                .map(DisbursementSummaryDTO::getCommission)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new RestaurantDisbursementSummaryDTO(
                                restaurantId,
                                dtos.get(0).getRestaurantName(),
                                totalDisbursed,
                                totalCommission,
                                (long) dtos.size(),
                                dtos);
        }

}
