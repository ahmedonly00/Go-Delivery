package com.goDelivery.goDelivery.dtos.momo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoPaymentResponse {
    private String referenceId;
    private String status;
    private String message;
    private Double amount;
    private String currency;
    private String externalId;

    public static MomoPaymentResponse success(String externalId, Double amount) {
        return new MomoPaymentResponse(
            UUID.randomUUID().toString(),
            "PENDING",
            "Payment request initiated successfully",
            amount,
            "RWF",
            externalId
        );
    }
}
