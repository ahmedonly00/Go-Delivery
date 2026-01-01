package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.location.AddressRequest;
import com.goDelivery.goDelivery.dtos.location.AddressResponse;
import com.goDelivery.goDelivery.dtos.location.CityResponse;
import com.goDelivery.goDelivery.dtos.location.CountryResponse;
import com.goDelivery.goDelivery.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Locations", description = "Locations management")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/getAllCountries")
    public ResponseEntity<Map<String, Object>> getAllCountries() {
        try {
            List<CountryResponse> countries = locationService.getAllCountries();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Countries retrieved successfully");
            response.put("data", countries);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve countries: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/countries/{countryId}/cities")
    public ResponseEntity<Map<String, Object>> getCitiesByCountry(@PathVariable Long countryId) {
        try {
            List<CityResponse> cities = locationService.getCitiesByCountry(countryId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cities retrieved successfully");
            response.put("data", cities);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve cities: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping(value = "/createAddresses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createAddress(
            @RequestParam("customerId") Long customerId,
            @RequestParam("cityId") Long cityId,
            @RequestParam("street") String street,
            @RequestParam("areaName") String areaName,
            @RequestParam("houseNumber") String houseNumber,
            @RequestParam("localContactNumber") String localContactNumber,
            @RequestParam("latitude") Float latitude,
            @RequestParam("longitude") Float longitude,
            @RequestParam("addressType") String addressType,
            @RequestParam("usageOption") String usageOption,
            @RequestParam(value = "isDefault", defaultValue = "false") Boolean isDefault,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        try {
            // Build AddressRequest from form parameters
            AddressRequest request = AddressRequest.builder()
                    .customerId(customerId)
                    .cityId(cityId)
                    .street(street)
                    .areaName(areaName)
                    .houseNumber(houseNumber)
                    .localContactNumber(localContactNumber)
                    .latitude(latitude)
                    .longitude(longitude)
                    .addressType(com.goDelivery.goDelivery.Enum.AddressType.valueOf(addressType.toUpperCase()))
                    .usageOption(usageOption)
                    .isDefault(isDefault)
                    .build();

            AddressResponse addressResponse = locationService.createAddress(request, image);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Address created successfully");
            response.put("data", addressResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create address: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getCustomerAddresses")
    public ResponseEntity<Map<String, Object>> getCustomerAddresses(@RequestParam Long customerId) {
        try {
            List<AddressResponse> addresses = locationService.getCustomerAddresses(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Addresses retrieved successfully");
            response.put("data", addresses);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve addresses: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<Map<String, Object>> getAddressById(@PathVariable Long addressId) {
        try {
            AddressResponse address = locationService.getAddressById(addressId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Address retrieved successfully");
            response.put("data", address);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve address: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/deleteAddress/{addressId}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable Long addressId) {
        try {
            locationService.deleteAddress(addressId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Address deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete address: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
