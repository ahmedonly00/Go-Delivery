package com.goDelivery.goDelivery.dtos.payment;

import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Payment method is required")
    private PaymentMenthod paymentMethod;
    
    @NotBlank(message = "Payment provider is required")
    private String paymentProvider;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    @NotBlank(message = "Reference number is required")
    private String referenceNumber;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Float amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotBlank(message = "Gateway response is required")
    private String gatewayResponse;
}
