package com.goDelivery.goDelivery.dtos.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class MpesaTransactionStatus {
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("amount")
    private Float amount;
    
    @JsonProperty("msisdn")
    private String msisdn;
    
    @JsonProperty("thirdPartyRef")
    private String thirdPartyRef;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("transactionStatus")
    private String transactionStatus;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
   
    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(status) || "SUCCESSFUL".equalsIgnoreCase(transactionStatus);
    }
    
    
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(transactionStatus);
    }
    
    
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(transactionStatus);
    }
}
