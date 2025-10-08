package com.goDelivery.goDelivery.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.goDelivery.goDelivery.dtos.customer.CustomerRegistrationRequest;
import com.goDelivery.goDelivery.dtos.customer.CustomerResponse;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.CustomerMapper;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    public CustomerResponse registerCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        Customer customer = customerMapper.toEntity(customerRegistrationRequest);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
        
    }

    public CustomerResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        return customerMapper.toResponse(customer);
    }

    public CustomerResponse updateCustomer(Long customerId, CustomerRegistrationRequest customerRegistrationRequest) {
        Customer existingCustomer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        
        existingCustomer.setFullNames(customerRegistrationRequest.getFullNames());
        existingCustomer.setLocation(customerRegistrationRequest.getLocation());
        existingCustomer.setEmail(customerRegistrationRequest.getEmail());
        existingCustomer.setPassword(passwordEncoder.encode(customerRegistrationRequest.getPassword()));
        existingCustomer.setConfirmPassword(passwordEncoder.encode(customerRegistrationRequest.getConfirmPassword()));
        existingCustomer.setPhoneNumber(customerRegistrationRequest.getPhoneNumber());
        existingCustomer.setActive(true);
        existingCustomer.setLastLogin(LocalDate.now());
        existingCustomer.setCreatedAt(LocalDate.now());
        existingCustomer.setUpdatedAt(LocalDate.now());
        return customerMapper.toResponse(customerRepository.save(existingCustomer));
    }   

    public void deleteCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
        .stream()
        .map(customerMapper::toResponse)
        .collect(Collectors.toList());
    }

    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return customerMapper.toResponse(customer);
    }

    public CustomerResponse getCustomerProfile(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return customerMapper.toResponse(customer);
    }

}
