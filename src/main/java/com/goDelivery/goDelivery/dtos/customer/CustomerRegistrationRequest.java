package com.goDelivery.goDelivery.dtos.customer;

import com.goDelivery.goDelivery.Enum.Roles;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegistrationRequest {

    @NotBlank(message = "Full name is required")
    @JsonProperty("fullNames")
    private String fullNames;

    @NotBlank(message = "Location is required")
    @JsonProperty("location")
    private String location;

    @NotBlank(message = "Phone number is required")
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @JsonProperty("email")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty("password")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @JsonProperty("confirmPassword")
    private String confirmPassword;
    
    @NotNull(message = "Roles is required")
    @Enumerated(EnumType.STRING)
    @JsonProperty("roles")
    private Roles roles;
}       
