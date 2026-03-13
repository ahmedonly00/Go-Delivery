package com.goDelivery.goDelivery.modules.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RestaurantAdminCredentials {
    private String username;
    private String password;
}
