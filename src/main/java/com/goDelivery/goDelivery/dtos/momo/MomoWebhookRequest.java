package com.goDelivery.goDelivery.dtos.momo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoWebhookRequest {
    @JsonProperty("referenceId")
    private String referenceId;
    
    @JsonProperty("externalId")
    private String externalId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("financialTransactionId")
    private String financialTransactionId;
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("reason")
    private Reason reason;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reason {
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("message")
        private String message;
    }
}
