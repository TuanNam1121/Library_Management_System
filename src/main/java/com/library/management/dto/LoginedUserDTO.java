package com.library.management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class LoginedUserDTO {
    private String name;
    private String role;
    private String email;
}
