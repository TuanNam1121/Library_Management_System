package com.library.management.services;

import com.library.management.dto.ChangePasswordDTO;
import com.library.management.dto.LoginRequestDTO;
import com.library.management.dto.LoginedUserDTO;
import com.library.management.dto.RegisterRequestDTO;

import com.library.management.dto.UpdateProfileDTO;
import com.library.management.entities.User;

import java.util.List;

public interface UserService {
    void register(RegisterRequestDTO registerRequest);
    User login(LoginRequestDTO loginRequestDTO);
    User getByUsername(String username);
    void updateProfile(String username, UpdateProfileDTO dto);
    void changePassword(String username, ChangePasswordDTO dto);

    LoginedUserDTO getLoginUser(LoginRequestDTO loginRequestDTO);

    List<User> findAllReader();

    List<User> searchReader(String keyword);

    void update(User user);

    User findById(long id);

    boolean searchUser(User oldUser, User newUser);
}
