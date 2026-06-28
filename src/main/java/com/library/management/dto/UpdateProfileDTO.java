package com.library.management.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProfileDTO {
    private String fullName;
    private String email;
    private String phone;
    private String address;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    private MultipartFile avatarFile;
}
