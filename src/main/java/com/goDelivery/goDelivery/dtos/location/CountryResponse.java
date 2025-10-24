package com.goDelivery.goDelivery.dtos.location;

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
