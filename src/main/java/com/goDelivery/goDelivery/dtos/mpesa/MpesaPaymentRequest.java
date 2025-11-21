package com.goDelivery.goDelivery.dtos.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Data
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class MpesaPaymentRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^258\\d{9}$", message = "Phone number must be in format 258XXXXXXXXX")
    @JsonProperty("fromMSISDN")
    private String fromMsisdn;
    
    @NotNull(message = "Amount is required")
    @JsonProperty("amount")
    private Double amount;
    
    private String callback;
    
    @JsonProperty("thirdPartyRef")
    private String thirdPartyRef;
    
    private String description;
    
    @JsonProperty("orderId")
    private Long orderId;

    @Override
    public String toString() {
        return "MpesaPaymentRequest{" +
                "fromMSISDN='" + fromMsisdn + '\'' +
                ", amount=" + amount +
                ", callback='" + callback + '\'' +
                ", thirdPartyRef='" + thirdPartyRef + '\'' +
                ", description='" + description + '\'' +
                ", orderId=" + orderId +
                '}';
    }
}
