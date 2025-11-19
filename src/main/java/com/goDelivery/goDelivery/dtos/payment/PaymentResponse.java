package com.goDelivery.goDelivery.dtos.payment;

import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private PaymentMenthod paymentMethod;
    private String paymentProvider;
    private String phoneNumber;
    private String transactionId;
    private String referenceNumber;
    private Float amount;
    private String currency;
    private PaymentStatus paymentStatus;
    private String gateWayResponse;
    private String failureReason;
    private LocalDate paymentDate;
    private LocalDate createdAt;
    
    // Related DTOs
    private SimpleOrderDto order;
    
    private String message;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleOrderDto {
        private Long orderId;
        private String orderNumber;
        private String status;
    }
}
