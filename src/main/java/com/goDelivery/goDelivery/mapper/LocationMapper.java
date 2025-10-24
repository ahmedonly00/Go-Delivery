package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.location.AddressRequest;
import com.goDelivery.goDelivery.dtos.location.AddressResponse;
import com.goDelivery.goDelivery.dtos.location.CityResponse;
import com.goDelivery.goDelivery.dtos.location.CountryResponse;
import com.goDelivery.goDelivery.model.City;
import com.goDelivery.goDelivery.model.Country;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.CustomerAddress;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationMapper {

    // Convert Country entity to CountryResponse DTO
    public CountryResponse toCountryResponse(Country country) {
        if (country == null) {
            return null;
        }

        return CountryResponse.builder()
                .countryId(country.getCountryId())
                .countryName(country.getCountryName())
                .countryCode(country.getCountryCode())
                .build();
    }

    // Convert list of Country entities to list of CountryResponse DTOs
    public List<CountryResponse> toCountryResponseList(List<Country> countries) {
        if (countries == null) {
            return null;
        }

        return countries.stream()
                .map(this::toCountryResponse)
                .collect(Collectors.toList());
    }

    // Convert City entity to CityResponse DTO
    public CityResponse toCityResponse(City city) {
        if (city == null) {
            return null;
        }

        return CityResponse.builder()
                .cityId(city.getCityId())
                .cityName(city.getCityName())
                .countryId(city.getCountry() != null ? city.getCountry().getCountryId() : null)
                .build();
    }

    // Convert list of City entities to list of CityResponse DTOs
    public List<CityResponse> toCityResponseList(List<City> cities) {
        if (cities == null) {
            return null;
        }

        return cities.stream()
                .map(this::toCityResponse)
                .collect(Collectors.toList());
    }

    // Convert AddressRequest DTO to CustomerAddress entity
    public CustomerAddress toAddressEntity(AddressRequest request, Customer customer, City city) {
        if (request == null) {
            return null;
        }

        return CustomerAddress.builder()
                .customer(customer)
                .city(city)
                .street(request.getStreet())
                .areaName(request.getAreaName())
                .houseNumber(request.getHouseNumber())
                .localContactNumber(request.getLocalContactNumber())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .addressType(request.getAddressType())
                .usageOption(request.getUsageOption())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();
    }

    // Convert CustomerAddress entity to AddressResponse DTO
    public AddressResponse toAddressResponse(CustomerAddress address) {
        if (address == null) {
            return null;
        }

        return AddressResponse.builder()
                .customerAddressId(address.getCustomerAddressId())
                .customerId(address.getCustomer() != null ? address.getCustomer().getCustomerId() : null)
                .cityId(address.getCity() != null ? address.getCity().getCityId() : null)
                .cityName(address.getCity() != null ? address.getCity().getCityName() : null)
                .street(address.getStreet())
                .areaName(address.getAreaName())
                .houseNumber(address.getHouseNumber())
                .localContactNumber(address.getLocalContactNumber())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .addressType(address.getAddressType())
                .usageOption(address.getUsageOption())
                .imageUrl(address.getImageUrl())
                .isDefault(address.isDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    // Convert list of CustomerAddress entities to list of AddressResponse DTOs
    public List<AddressResponse> toAddressResponseList(List<CustomerAddress> addresses) {
        if (addresses == null) {
            return null;
        }

        return addresses.stream()
                .map(this::toAddressResponse)
                .collect(Collectors.toList());
    }
}
