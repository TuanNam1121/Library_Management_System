package com.library.management.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterRequestDTO {
    @NotBlank(message = "Username is required")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9_]{4,19}$",
            message = "Username must start with a letter and contain only letters, numbers, or underscore (5-20 characters)"
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 50)
    private String password;

    @NotBlank
    @Size(min = 6, max = 50)
    private String confirmPassword;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(min = 5, max = 100)
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(min = 5, max = 100,message = "full name must be 5 and 100 characters")
    private String fullName;

    @NotBlank(message = "phone is required" )
    @Pattern(
            regexp = "^0\\d{9}$",
            message = "Phone number must start with 0 and contain 10-11 digits"
    )
    private String phone;

    @NotBlank(message = "address is required")
    @Size(min = 5, max = 255,
    message = "Address must be between 5 and 255 characters")
    private String address;

    @NotNull(message = "Day of birth is required")
    @Past(message = "Date of bird must be in part")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
}
