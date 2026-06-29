package com.library.management.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProfileDTO {
    @NotBlank(message = "phone is required" )
    @Pattern(
            regexp = "^0\\d{9}$",
            message = "Phone number must start with 0 and contain 10-11 digits"
    )
    private String phone;
    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 255,
            message = "Address must be between 5 and 255 characters")
    private String address;
    private MultipartFile avatarFile;
}
