package com.goDelivery.goDelivery.dtos.customer;

import com.goDelivery.goDelivery.Enum.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long customerId;
    private String fullNames;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String profileImage;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean isActive;
    private LocalDate lastLogin;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
