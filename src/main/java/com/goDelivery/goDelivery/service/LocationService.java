package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.location.AddressRequest;
import com.goDelivery.goDelivery.dtos.location.AddressResponse;
import com.goDelivery.goDelivery.dtos.location.CityResponse;
import com.goDelivery.goDelivery.dtos.location.CountryResponse;
import com.goDelivery.goDelivery.mapper.LocationMapper;
import com.goDelivery.goDelivery.model.City;
import com.goDelivery.goDelivery.model.Country;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.CustomerAddress;
import com.goDelivery.goDelivery.repository.CityRepository;
import com.goDelivery.goDelivery.repository.CountryRepository;
import com.goDelivery.goDelivery.repository.CustomerAddressRepository;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final CustomerAddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final LocationMapper locationMapper;
    private final FileStorageService fileStorageService;

   
    public List<CountryResponse> getAllCountries() {
        List<Country> countries = countryRepository.findAll();
        return locationMapper.toCountryResponseList(countries);
    }

    
    public List<CityResponse> getCitiesByCountry(Long countryId) {
        // Verify country exists
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new RuntimeException("Country not found with ID: " + countryId));

        List<City> cities = cityRepository.findByCountryCountryId(countryId);
        return locationMapper.toCityResponseList(cities);
    }

    
    @Transactional
    public AddressResponse createAddress(AddressRequest request, MultipartFile image) {
        // Verify customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));

        // Verify city exists
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new RuntimeException("City not found with ID: " + request.getCityId()));

        // If this address is set as default, unset other default addresses
        if (request.getIsDefault() != null && request.getIsDefault()) {
            List<CustomerAddress> existingAddresses = addressRepository.findByCustomerCustomerId(request.getCustomerId());
            existingAddresses.forEach(addr -> {
                addr.setDefault(false);
                addressRepository.save(addr);
            });
        }

        // Create address entity
        CustomerAddress address = locationMapper.toAddressEntity(request, customer, city);

        // Handle image upload if provided
        if (image != null && !image.isEmpty()) {
            String imagePath = fileStorageService.storeFile(image, "addresses");
            address.setImageUrl("/uploads/" + imagePath);
        }

        // Save address
        CustomerAddress savedAddress = addressRepository.save(address);

        return locationMapper.toAddressResponse(savedAddress);
    }

    
    public List<AddressResponse> getCustomerAddresses(Long customerId) {
        // Verify customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        List<CustomerAddress> addresses = addressRepository.findByCustomerCustomerIdOrderByIsDefaultDesc(customerId);
        return locationMapper.toAddressResponseList(addresses);
    }

   
    public AddressResponse getAddressById(Long addressId) {
        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));

        return locationMapper.toAddressResponse(address);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));

        addressRepository.delete(address);
    }
}
