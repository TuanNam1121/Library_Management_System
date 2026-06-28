package com.library.management.services;

import com.library.management.dto.ChangePasswordDTO;
import com.library.management.dto.LoginRequestDTO;
import com.library.management.dto.LoginedUserDTO;
import com.library.management.dto.RegisterRequestDTO;

import com.library.management.dto.UpdateProfileDTO;
import com.library.management.entities.User;

public interface UserService {
    void register(RegisterRequestDTO registerRequest);
    User login(LoginRequestDTO loginRequestDTO);
    User getByUsername(String username);
    void updateProfile(String username, UpdateProfileDTO dto);
    void changePassword(String username, ChangePasswordDTO dto);

    LoginedUserDTO getLoginUser(LoginRequestDTO loginRequestDTO);
}
