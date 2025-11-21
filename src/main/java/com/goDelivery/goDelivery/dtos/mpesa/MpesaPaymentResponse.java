package com.goDelivery.goDelivery.dtos.mpesa;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MpesaPaymentResponse {
    private String transactionId;
    private Float amount;
    private String transactionStatus;
    private String msisdn;
    private String thirdPartyRef;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;
}
