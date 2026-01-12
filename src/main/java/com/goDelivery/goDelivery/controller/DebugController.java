package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.service.DisbursementService;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.dtos.momo.collectionDisbursement.CollectionDisbursementResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final DisbursementService disbursementService;
    private final OrderRepository orderRepository;

    @GetMapping("/permissions")
    public Map<String, Object> getCurrentUserPermissions() {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        response.put("isAuthenticated", auth.isAuthenticated());
        
        return response;
    }
    
    @PostMapping("/trigger-disbursement/{orderId}")
    @Operation(summary = "Manually trigger disbursement for an order (DEBUG)")
    public ResponseEntity<Map<String, Object>> triggerDisbursement(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("=== MANUAL DISBURSEMENT TRIGGER FOR ORDER {} ===", orderId);
            
            var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            
            log.info("Order found: {}", order.getOrderNumber());
            log.info("Order status: {}", order.getOrderStatus());
            log.info("Payment status: {}", order.getPaymentStatus());
            log.info("Total amount: {}", order.getTotalAmount());
            
            if (order.getPaymentStatus() != com.goDelivery.goDelivery.Enum.PaymentStatus.PAID) {
                response.put("error", "Order is not paid. Current status: " + order.getPaymentStatus());
                return ResponseEntity.badRequest().body(response);
            }
            
            CollectionDisbursementResponse disbursementResponse = disbursementService.processOrderDisbursement(order);
            
            response.put("success", true);
            response.put("disbursementReference", disbursementResponse.getReferenceId());
            response.put("message", "Disbursement initiated successfully");
            
            log.info("Disbursement initiated with reference: {}", disbursementResponse.getReferenceId());
            
        } catch (Exception e) {
            log.error("Failed to trigger disbursement", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check-order/{orderId}")
    @Operation(summary = "Check order details and disbursements")
    public ResponseEntity<Map<String, Object>> checkOrder(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            
            response.put("orderId", order.getOrderId());
            response.put("orderNumber", order.getOrderNumber());
            response.put("orderStatus", order.getOrderStatus());
            response.put("paymentStatus", order.getPaymentStatus());
            response.put("totalAmount", order.getTotalAmount());
            response.put("updatedAt", order.getUpdatedAt());
            
            // Get restaurant details
            if (order.getRestaurantOrders() != null) {
                response.put("restaurantCount", order.getRestaurantOrders().size());
                order.getRestaurantOrders().forEach(ro -> {
                    log.info("Restaurant: {}, Phone: {}", 
                            ro.getRestaurant().getRestaurantName(), 
                            ro.getRestaurant().getPhoneNumber());
                });
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
