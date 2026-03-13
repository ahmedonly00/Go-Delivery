package com.goDelivery.goDelivery.modules.customer.service;

import com.goDelivery.goDelivery.modules.customer.dto.CustomerRegistrationRequest;
import com.goDelivery.goDelivery.modules.customer.dto.CustomerResponse;
import com.goDelivery.goDelivery.modules.customer.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRegistrationRequest request) {
        Customer customer = new Customer();
        customer.setFullNames(request.getFullNames());
        customer.setLocation(request.getLocation());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setPassword(request.getPassword());
        customer.setConfirmPassword(request.getConfirmPassword());
        customer.setRoles(request.getRoles());
        return customer;
    }

    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .fullNames(customer.getFullNames())
                .location(customer.getLocation())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .roles(customer.getRoles())
                .build();
    }
}
