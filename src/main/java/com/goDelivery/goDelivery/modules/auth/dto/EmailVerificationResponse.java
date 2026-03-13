package com.goDelivery.goDelivery.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationResponse {
    private boolean success;
    private String message;
    private String redirectUrl;
    private Long restaurantId;
    private String restaurantName;
}
