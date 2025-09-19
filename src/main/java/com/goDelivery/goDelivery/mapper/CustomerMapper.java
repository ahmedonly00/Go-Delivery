package com.goDelivery.goDelivery.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.goDelivery.goDelivery.dtos.customer.CustomerRegistrationRequest;
import com.goDelivery.goDelivery.dtos.customer.CustomerResponse;
import com.goDelivery.goDelivery.dtos.customer.CustomerUpdateRequest;
import com.goDelivery.goDelivery.model.Customer;

@Component
public class CustomerMapper {

    //convert customer registration request to customer entity
    public Customer toEntity(CustomerRegistrationRequest customerRegistrationRequest) {
        if (customerRegistrationRequest == null) {
            return null;
        }

        return Customer.builder()
                .fullNames(customerRegistrationRequest.getFullNames())
                .email(customerRegistrationRequest.getEmail())
                .password(customerRegistrationRequest.getPassword())
                .phoneNumber(customerRegistrationRequest.getPhoneNumber())
                .dateOfBirth(customerRegistrationRequest.getDateOfBirth())
                .gender(customerRegistrationRequest.getGender())
                .roles(customerRegistrationRequest.getRoles())
                .build();
        
    }


    //convert customer to customer response
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .fullNames(customer.getFullNames())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .roles(customer.getRoles())
                .emailVerified(customer.isEmailVerified())
                .phoneVerified(customer.isPhoneVerified())
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
        customer.setDateOfBirth(customerUpdateRequest.getDateOfBirth());
        customer.setGender(customerUpdateRequest.getGender());

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
