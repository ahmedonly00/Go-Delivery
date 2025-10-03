package com.goDelivery.goDelivery.dtos.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantAdminResponseDTO {
    private Long userId;
    private String email;
    private boolean emailVerified;
    private boolean setupComplete;
    private String message;
}
