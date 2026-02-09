package com.goDelivery.goDelivery.dtos.user;

import com.goDelivery.goDelivery.Enum.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerUpdateRequest {
    private String fullNames;

    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,5}[-\\s\\.]?[0-9]{1,6}$", message = "Phone number must be 10-15 digits and may include country code (e.g., +258 84 123 4567, 258-84-123-4567)")
    private String phoneNumber;

    private LocalDate dateOfBirth;

    private Gender gender;

    private String profileImage;

    @Size(min = 6, message = "Current password must be at least 6 characters")
    private String currentPassword;

    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;
}
