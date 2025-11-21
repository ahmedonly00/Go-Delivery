package com.goDelivery.goDelivery.dtos.mpesa;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

@Data
public class MpesaPaymentRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^258\\d{9}$", message = "Phone number must be in format 258XXXXXXXXX")
    private String fromMSISDN;
    
    @NotNull(message = "Amount is required")
    private Float amount;
    
    private String callback;
    
    private Long orderId;
    
    private String thirdPartyRef;

    @Override
    public String toString() {
        return "MpesaPaymentRequest{" +
                "fromMSISDN='" + fromMSISDN + '\'' +
                ", amount=" + amount +
                ", callback='" + callback + '\'' +

                ", orderId=" + orderId +
                ", thirdPartyRef='" + thirdPartyRef + '\'' +
                '}';
    }
}
