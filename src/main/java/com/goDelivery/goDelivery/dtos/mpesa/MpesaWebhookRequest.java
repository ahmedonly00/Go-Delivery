package com.goDelivery.goDelivery.dtos.mpesa;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MpesaWebhookRequest {
    @JsonProperty("transactionId")
    private String transactionId;
    
    private Double amount;
    private String msisdn;
    private String description;
    private String code;
    private String thirdPartyRef;
    private String transactionStatus;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;
    
    // Helper method to check if transaction was successful
    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(transactionStatus);
    }
    
    // Helper method to check if transaction is pending
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(transactionStatus);
    }
    
    // Helper method to check if transaction failed
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(transactionStatus) || 
               "EXPIRED".equalsIgnoreCase(transactionStatus);
    }
}
