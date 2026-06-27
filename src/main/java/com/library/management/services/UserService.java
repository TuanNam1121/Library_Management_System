package com.library.management.services;

import com.library.management.dto.LoginRequestDTO;
import com.library.management.dto.RegisterRequestDTO;

public interface UserService {
    void register(RegisterRequestDTO registerRequest);
    boolean login(LoginRequestDTO loginRequestDTO);
}
