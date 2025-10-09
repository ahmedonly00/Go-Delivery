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
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;

    @SuppressWarnings("unused")
    @Transactional
    public CustomerResponse registerCustomer(CustomerRegistrationRequest request) {
        log.info("Starting registration for email: {}", request.getEmail());
        
        // Validate request
        if (request == null) {
            log.error("Registration request is null");
            throw new IllegalArgumentException("Registration request cannot be null");
        }
        
        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new IllegalStateException("Email already registered");
        }
        
        try {
            // Map and save the customer
            Customer customer = customerMapper.toEntity(request);
            customer.setActive(false); // Set to false until email is verified
            customer.setVerified(false);
            customer.setEmailVerified(false);
            customer.setPhoneVerified(false);
            
            // Encode password
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            customer.setPassword(encodedPassword);
            customer.setConfirmPassword(encodedPassword);
            
            // Set creation and update timestamps
            LocalDate now = LocalDate.now();
            customer.setCreatedAt(now);
            customer.setUpdatedAt(now);
            
            // Save customer first to get the ID
            Customer savedCustomer = customerRepository.save(customer);
            log.info("Customer registered successfully with ID: {}", savedCustomer.getId());
            
            // Generate and send OTP in a separate try-catch to not fail registration if email fails
            try {
                otpService.generateAndSaveOTP(savedCustomer);
                log.info("OTP sent to email: {}", savedCustomer.getEmail());
            } catch (Exception e) {
                log.error("Failed to send OTP email to: {}", savedCustomer.getEmail(), e);
                // Don't fail the registration if email sending fails
                // The user can request a new OTP later
            }
            
            return customerMapper.toResponse(savedCustomer);
            
        } catch (Exception e) {
            log.error("Error during registration for email: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to register customer: " + e.getMessage(), e);
        }
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
