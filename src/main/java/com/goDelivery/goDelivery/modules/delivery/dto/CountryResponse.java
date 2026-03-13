package com.goDelivery.goDelivery.modules.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountryResponse {
    private Long countryId;
    private String countryName;
    private String countryCode;
}
