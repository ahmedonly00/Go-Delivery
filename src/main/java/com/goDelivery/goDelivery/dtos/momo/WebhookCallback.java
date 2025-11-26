package com.goDelivery.goDelivery.dtos.momo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookCallback {
    private String referenceId;
    private String externalId;
    private String status;
    private String financialTransactionId;
    private Float amount;
    private String currency;
    private String type;
    private ReasonInfo reason;
    private String timestamp;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasonInfo {
        private String code;
        private String message;
    }
}
