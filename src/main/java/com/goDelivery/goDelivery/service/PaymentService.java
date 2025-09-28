package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.dtos.payment.PaymentRequest;
import com.goDelivery.goDelivery.dtos.payment.PaymentResponse;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.PaymentMapper;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.model.Payment;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        log.info("Processing payment for order ID: {}", paymentRequest.getOrderId());
        
        // Find the order
        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + paymentRequest.getOrderId()));
        
        // Create and save payment
        Payment payment = paymentMapper.toEntity(paymentRequest, order);
        
        try {
            // Process payment based on payment method
            switch (paymentRequest.getPaymentMethod()) {
                case MPESA:
                    return processMpesaPayment(payment);
                case EMOLA:
                    return processEmolaPayment(payment);
                case CARD:
                    return processCardPayment(payment);
                case CASH:
                    return processCashPayment(payment);
                default:
                    throw new IllegalArgumentException("Unsupported payment method: " + paymentRequest.getPaymentMethod());
            }
        } catch (Exception e) {
            log.error("Payment processing failed: {}", e.getMessage(), e);
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            payment = paymentRepository.save(payment);
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return paymentMapper.toDto(payment);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        log.info("Fetching payment for order ID: {}", orderId);
        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));
        return paymentMapper.toDto(payment);
    }

    private PaymentResponse processMpesaPayment(Payment payment) {
        log.info("Processing MPESA payment");
        // TODO: Implement actual MPESA integration
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setGateWayResponse("MPESA payment processed successfully");
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    private PaymentResponse processEmolaPayment(Payment payment) {
        log.info("Processing Emola payment");
        // TODO: Implement actual Emola integration
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setGateWayResponse("Emola payment processed successfully");
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    private PaymentResponse processCardPayment(Payment payment) {
        log.info("Processing Card payment");
        // TODO: Implement actual Card payment integration
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setGateWayResponse("Card payment processed successfully");
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    private PaymentResponse processCashPayment(Payment payment) {
        log.info("Processing Cash payment");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setGateWayResponse("Payment will be processed on delivery");
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }
}
