package com.library.management.services;

import com.library.management.dto.LoginRequestDTO;
import com.library.management.dto.LoginedUserDTO;
import com.library.management.dto.RegisterRequestDTO;
import com.library.management.entities.User;

public interface UserService {
    void register(RegisterRequestDTO registerRequest);
    boolean login(LoginRequestDTO loginRequestDTO);
    LoginedUserDTO getLoginUser(LoginRequestDTO loginRequestDTO);
}
