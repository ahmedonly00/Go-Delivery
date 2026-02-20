package com.goDelivery.goDelivery.dtos.momo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoPaymentRequest {

    // Only orderId is required — the collection-disbursement API resolves all
    // other details (customer MSISDN, amount, recipients) from the order itself.
    @NotNull(message = "Order ID is required")
    private Long orderId;

    // Optional fields kept for backward compatibility
    private String externalId;
    private String msisdn;
    private Float amount;
    private String payerMessageTitle;
    private String payerMessageDescription;
    private String callback;
}
