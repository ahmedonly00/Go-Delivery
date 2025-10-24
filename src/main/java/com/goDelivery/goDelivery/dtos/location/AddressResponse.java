package com.goDelivery.goDelivery.dtos.location;

import com.goDelivery.goDelivery.Enum.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    private Long customerAddressId;
    private Long customerId;
    private Long cityId;
    private String cityName;
    private String street;
    private String areaName;
    private String houseNumber;
    private String localContactNumber;
    private Float latitude;
    private Float longitude;
    private AddressType addressType;
    private String usageOption;
    private String imageUrl;
    private Boolean isDefault;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
