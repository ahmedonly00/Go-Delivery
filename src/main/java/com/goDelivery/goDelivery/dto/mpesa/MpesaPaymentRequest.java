package com.goDelivery.goDelivery.dto.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MpesaPaymentRequest {
    @JsonProperty("fromMSISDN")
    private String fromMsisdn;
    
    private BigDecimal amount;
    
    private String callback;
    
    @JsonProperty("thirdPartyRef")
    private String thirdPartyRef;
    
    private String description;
}
