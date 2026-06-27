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
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "phone is required")
    private String phone;

    @NotBlank(message = "address is required")
    private String address;

    @NotNull(message = "Day of birth is required")
    @Past(message = "Date of bird must be in part")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
}
