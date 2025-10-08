package com.goDelivery.goDelivery.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.goDelivery.goDelivery.dtos.customer.CustomerRegistrationRequest;
import com.goDelivery.goDelivery.dtos.customer.CustomerResponse;
import com.goDelivery.goDelivery.dtos.customer.CustomerUpdateRequest;
import com.goDelivery.goDelivery.model.Customer;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomerMapper {

    private final PasswordEncoder passwordEncoder;

    //convert customer registration request to customer entity
    public Customer toEntity(CustomerRegistrationRequest customerRegistrationRequest) {
        if (customerRegistrationRequest == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setFullNames(customerRegistrationRequest.getFullNames());
        customer.setLocation(customerRegistrationRequest.getLocation());
        customer.setEmail(customerRegistrationRequest.getEmail());
        customer.setPhoneNumber(customerRegistrationRequest.getPhoneNumber());
        customer.setPassword(passwordEncoder.encode(customerRegistrationRequest.getPassword()));
        customer.setConfirmPassword(passwordEncoder.encode(customerRegistrationRequest.getConfirmPassword()));
        customer.setRoles(customerRegistrationRequest.getRoles());
        customer.setEmailVerified(true);
        customer.setPhoneVerified(true);
        customer.setLastLogin(LocalDate.now());
        customer.setCreatedAt(LocalDate.now());
        customer.setUpdatedAt(LocalDate.now());
        return customer; 
        
    }

    //convert customer to customer response
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .fullNames(customer.getFullNames())
                .location(customer.getLocation())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .password(passwordEncoder.encode(customer.getPassword()))
                .confirmPassword(passwordEncoder.encode(customer.getConfirmPassword()))
                .roles(customer.getRoles())
                .emailVerified(true)
                .phoneVerified(true)
                .isActive(true)
                .lastLogin(LocalDate.now())
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        
    }

    //convert customer update request to customer entity
    public Customer toUpdateEntity(CustomerUpdateRequest customerUpdateRequest) {
        if (customerUpdateRequest == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setFullNames(customerUpdateRequest.getFullNames());
        customer.setEmail(customerUpdateRequest.getEmail());
        customer.setPhoneNumber(customerUpdateRequest.getPhoneNumber());
        customer.setUpdatedAt(LocalDate.now());

        return customer;

        
    }

    //convert list of customers to list of customer responses
    public List<CustomerResponse> toResponse(List<Customer> customers) {
        if (customers == null) {
            return null;
        }

        return customers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    
}
