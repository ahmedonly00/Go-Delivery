package com.goDelivery.goDelivery.dtos.momo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoPaymentResponse {
    private String referenceId;
    private boolean success;
    private String status;
    private String message;
    private Float amount;
    private String currency;
    private String externalId;

    public static MomoPaymentResponse success(String referenceId, String externalId, Float amount) {
        MomoPaymentResponse response = new MomoPaymentResponse();
        response.setSuccess(true);
        response.setReferenceId(referenceId);
        response.setExternalId(externalId);
        response.setAmount(amount);
        response.setMessage("Payment request initiated successfully");
        return response;
    }
    
    public static MomoPaymentResponse error(String errorMessage) {
        MomoPaymentResponse response = new MomoPaymentResponse();
        response.setSuccess(false);
        response.setMessage(errorMessage);
        return response;
    }
}
