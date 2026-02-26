package com.goDelivery.goDelivery.dtos.momo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for initiating a MoMo payment")
public class MomoPaymentRequest {

    @Schema(description = "The unique ID of the order to pay for", example = "1234")
    @NotNull(message = "Order ID is required")
    private Long orderId;

    @Schema(description = "Optional external ID for the transaction", example = "TX-9988")
    private String externalId;

    @Schema(description = "Optional MSISDN to collect payment from. If provided, overrides the customer's registered number.", example = "250790000000")
    private String msisdn;

    @Schema(description = "Optional custom amount to collect", example = "500.0")
    private Float amount;

    @Schema(description = "Title for the payment message shown to the payer", example = "MozFood Payment")
    private String payerMessageTitle;

    @Schema(description = "Description for the payment message shown to the payer", example = "Payment for Order #1234")
    private String payerMessageDescription;

    @Schema(description = "Custom callback URL for status updates")
    private String callback;
}
