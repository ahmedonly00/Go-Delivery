package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.payment.PaymentRequest;
import com.goDelivery.goDelivery.dtos.payment.PaymentResponse;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.model.Payment;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PaymentMapper {

    // convert payment request to payment
    public Payment toEntity(PaymentRequest request, Order order) {
        if (request == null) {
            return null;
        }

        return Payment.builder()
                .paymentMenthod(request.getPaymentMethod())
                .paymentProvider(request.getPaymentProvider())
                .transactionId(request.getTransactionId())
                .referenceNumber(request.getReferenceNumber())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentStatus(PaymentStatus.PENDING) // Default status
                .gateWayResponse(request.getGatewayResponse())
                .failureReason("") // Initialize empty failure reason
                .paymentDate(LocalDate.now())
                .order(order)
                .build();
    }

    // convert payment to payment response
    public PaymentResponse toDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentResponse.SimpleOrderDto orderDto = null;
        if (payment.getOrder() != null) {
            orderDto = PaymentResponse.SimpleOrderDto.builder()
                    .orderId(payment.getOrder().getOrderId())
                    .orderNumber(String.valueOf(payment.getOrder().getOrderNumber()))
                    .status(payment.getOrder().getOrderStatus().name())
                    .build();
        }

        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .paymentMethod(payment.getPaymentMenthod())
                .paymentProvider(payment.getPaymentProvider())
                .transactionId(payment.getTransactionId())
                .referenceNumber(payment.getReferenceNumber())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentStatus(payment.getPaymentStatus())
                .gateWayResponse(payment.getGateWayResponse())
                .failureReason(payment.getFailureReason())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .order(orderDto)
                .build();
    }
}
